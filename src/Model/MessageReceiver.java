package Model;

import Helper.Aes;
import Helper.Status;

import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by karui on 2016-10-03.
 */
public class MessageReceiver extends Observable implements Runnable {
    private final int BUFFER_SIZE = 25600;
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
                    byte[] receivedBytes = new byte[input.available()];
                    input.readFully(receivedBytes);

                    byte[] senderIVBytes = Arrays.copyOfRange(receivedBytes, 0, 16);
                    byte[] ciphertextBytes = Arrays.copyOfRange(receivedBytes, 16, receivedBytes.length);
                    Vpn.getVpnManager().getIvManager().setIV(senderIVBytes);

                    String ciphertextString = DatatypeConverter.printHexBinary(ciphertextBytes);

                    Cipher cipher = Aes.getAesCipher(Cipher.DECRYPT_MODE, Vpn.getVpnManager().getSessionKey());
                    String plaintextString = Aes.decrypt(ciphertextBytes, cipher);

                    System.out.println("Received ciphertext: " + ciphertextString);
                    System.out.println("Received plaintext: " + plaintextString);
                    updateMsgReceived(plaintextString);
                }
            } catch (IOException e) {
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
