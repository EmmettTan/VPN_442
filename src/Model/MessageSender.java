package Model;

import Helper.Aes;
import Helper.Common;
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

            System.out.println("Plaintext is: " + textToSend);
            System.out.println("Ciphertext is: " + Common.bytesToHexString(ciphertextBytes));
            System.out.println("IV is: " + Common.bytesToHexString(ivByteArray));
            System.out.println("Sent message: " + Common.bytesToHexString(ciphertextIVBytes));

            writer.write(ciphertextIVBytes);
        } catch (IOException ex) {
            Vpn.getVpnManager().terminate();
            System.exit(1);
        }
    }
}
