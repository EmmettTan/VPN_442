package Model;

import Helper.Aes;
import Helper.Status;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by karui on 2016-10-03.
 */
public class MessageSender {
    String textToSend = "";

    public MessageSender(String textToSend) {
        this.textToSend = textToSend;
    }

    public void sendText() {
        Status status = Vpn.getVpnManager().getStatus();

        if (status != Status.BothConnected) {
            System.out.println("No client to send the message to.");
            return;
        }
        try {
            DataOutputStream writer = Vpn.getVpnManager().getWriter();
            byte[] ivByteArray = Vpn.getVpnManager().getIvManager().getIV();

            Cipher cipher = Aes.getAesCipher(Cipher.ENCRYPT_MODE, Vpn.getVpnManager().getSessionKey());
            byte[] ciphertextBytes = Aes.encrypt(textToSend, cipher);

            byte[] ciphertextIVBytes = new byte[ivByteArray.length + ciphertextBytes.length];
            System.arraycopy(ivByteArray, 0, ciphertextIVBytes, 0, ivByteArray.length);
            System.arraycopy(ciphertextBytes, 0, ciphertextIVBytes, ivByteArray.length, ciphertextBytes.length);

            String ciphertextString = DatatypeConverter.printHexBinary(ciphertextIVBytes);

            System.out.println("Plaintext is: " + textToSend);
            System.out.println("Encrypted message is: " + ciphertextString);

            writer.write(ciphertextIVBytes);
        } catch (IOException ex) {
            Vpn.getVpnManager().terminate();
            System.exit(1);
        }
    }
}
