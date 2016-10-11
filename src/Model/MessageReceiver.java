package Model;

import Helper.Common;
import Helper.Status;

import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by karui on 2016-10-03.
 */
public class MessageReceiver extends Observable implements Runnable {
    private String msgReceived;

    public MessageReceiver() {
        msgReceived = "";
        addObserver(Vpn.getVpnUi());
    }

    @Override
    public void run() {
        while (true) {
            DataInputStream input = Vpn.getVpnManager().getReader();
            try {
                if (input.available() != 0) {
                    byte[] ciphertextBytes = new byte[input.available()];
                    input.readFully(ciphertextBytes);

                    String ciphertextString = DatatypeConverter.printHexBinary(ciphertextBytes);

                    Cipher cipher = Common.getAesCipher(Cipher.DECRYPT_MODE);
                    ciphertextBytes = Common.setCorrectBlockLength(ciphertextBytes);

                    byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);
                    String plaintextString = new String(plaintextBytes, Common.ENCODING_TYPE);

                    System.out.println("Received ciphertext: " + ciphertextString);
                    System.out.println("Received plaintext: " + plaintextString);
                    updateMsgReceived(plaintextString);
                }
            } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                Vpn.getVpnManager().setStatus(Status.Disconnected);
                Vpn.getVpnManager().terminate();
                break;
            }
        }
    }

    private void updateMsgReceived(String plaintext) {
        msgReceived = plaintext;
        notifyAllObservers();
    }

    @Override
    public String getMessage() {
        return msgReceived;
    }
}
