package Model;

import Helper.Common;
import Helper.Status;

import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by karui on 2016-10-03.
 */
public class MessageReceiver implements Runnable {
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

                    Cipher cipher = Common.getAesCipher(Cipher.DECRYPT_MODE);
                    ciphertextBytes = Common.setCorrectBlockLength(ciphertextBytes);

                    byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);

                    System.out.println("Received ciphertext: " + ciphertextString);
                    System.out.println("Received plaintext: " + new String(plaintextBytes, "UTF-8"));
                }
            } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                Vpn.getVpnManager().setStatus(Status.Disconnected);
                Vpn.getVpnManager().terminate();
                break;
            }
        }
    }
}
