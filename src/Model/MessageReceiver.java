package Model;

import Helper.Aes;
import Helper.Common;
import Helper.Status;

import Ui.UpdateNames;

import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

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
                    byte[] receivedBytes = new byte[input.available()];
                    input.readFully(receivedBytes);

                    byte[] senderIVBytes = Arrays.copyOfRange(receivedBytes, 0, 16);
                    byte[] ciphertextBytes = Arrays.copyOfRange(receivedBytes, 16, receivedBytes.length);
                    Vpn.getVpnManager().getIvManager().setIV(senderIVBytes);

                    Cipher cipher = Aes.getAesCipher(Cipher.DECRYPT_MODE, Vpn.getVpnManager().getSessionKey());
                    String plaintextString = Aes.decrypt(ciphertextBytes, cipher);

                    System.out.println("Received IV + ciphertext: " + Common.bytesToHexString(receivedBytes));
                    System.out.println("Parsed IV: " + Common.bytesToHexString(senderIVBytes));
                    System.out.println("Parsed ciphertext: " + Common.bytesToHexString(ciphertextBytes));
                    System.out.println("Received plaintext: " + plaintextString);
                    updateMsgReceived(plaintextString);
                }
            } catch (IOException e) {
                Vpn.getVpnManager().setStatus(Status.Disconnected);
                System.out.println("Disconnected");
                Vpn.getVpnManager().terminate();
                System.exit(1);
            }
        }
    }

    private void updateMsgReceived(String plaintext) {
        msgReceived = plaintext;
        notifyAllObservers(plaintext, UpdateNames.MESSAGE);
    }

    @Override
    public String getMessage() {
        return msgReceived;
    }
}
