package Model;

import Helper.Common;
import Helper.Status;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.xml.bind.DatatypeConverter;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by karui on 2016-10-03.
 */
public class MessageSender {
    String textToSend = "";

    public MessageSender(String textToSend) {
        this.textToSend = /* concatanate iv */textToSend;
    }

    public void sendText() {
        Status status = Vpn.getVpnManager().getStatus();

        if (status != Status.BothConnected) {
            System.out.println("tell user they need to be connected");

            // comment out this return for testing purposes for now; we need this return in real application
            //return;
        }
        try {
            DataOutputStream writer = Vpn.getVpnManager().getWriter();
            byte[] ivByteArray = Vpn.getVpnManager().getIvManager().getIV();

            Cipher cipher = Common.getAesCipher(Cipher.ENCRYPT_MODE);
            byte[] plaintextBytes = textToSend.getBytes(Common.ENCODING_TYPE);

            plaintextBytes = Common.setCorrectBlockLength(plaintextBytes);

            byte[] ciphertextBytes = cipher.doFinal(plaintextBytes);


            byte[] ciphertextIVBytes = new byte[ivByteArray.length + ciphertextBytes.length];
            System.arraycopy(ivByteArray, 0, ciphertextIVBytes, 0, ivByteArray.length);
            System.arraycopy(ciphertextBytes, 0, ciphertextIVBytes, ivByteArray.length, ciphertextBytes.length);

            String ciphertextString = DatatypeConverter.printHexBinary(ciphertextIVBytes);

            System.out.println("Sent plaintext: " + textToSend);
            System.out.println("Sent ciphertext: " + ciphertextString);

            writer.write(ciphertextIVBytes);
        } catch (IOException | IllegalBlockSizeException | BadPaddingException ex) {
            Vpn.getVpnManager().terminate();
            ex.printStackTrace();
        }
    }
}
