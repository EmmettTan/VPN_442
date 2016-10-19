package Model;

import Helper.Aes;
import Helper.Common;
import Helper.Diffie;
import Helper.Status;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;
import java.security.MessageDigest;

/**
 * Created by karui on 2016-10-03.
 */
public class Client implements Runnable {

    private Socket clientSocket;

    protected Client() {
        System.out.println("Hi im a client");
    }

    @Override
    public void run() {
        try {
            byte[] nonce = Common.setupIdentity(clientSocket);
            DataInputStream reader = Vpn.getVpnManager().getReader();
            DataOutputStream writer = Vpn.getVpnManager().getWriter();

            byte[] identityBytes = Vpn.getVpnManager().getMyIdentity();
            byte[] bytesToSend = new byte[Common.NONCE_LENGTH + Common.IDENTITY_LENGTH];
            System.arraycopy(nonce, 0, bytesToSend, 0, nonce.length);
            System.out.println("Generated my nonce: " + Common.bytesToHexString(nonce));
            System.arraycopy(identityBytes, 0, bytesToSend, nonce.length, identityBytes.length);

            writer.write(bytesToSend);
            System.out.println("Sent my nonce and literal string CLNT: " + Common.bytesToHexString(bytesToSend));

            // wait for response from server
            while (reader.available() == 0) {}

            byte[] responseFromServer = new byte[reader.available()];
            reader.readFully(responseFromServer);

            // iv
            byte[] senderIVBytes = new byte[Common.IV_LENGTH];
            System.arraycopy(responseFromServer, 0, senderIVBytes, 0, Common.IV_LENGTH);
            Vpn.getVpnManager().getIvManager().setIV(senderIVBytes);
            System.out.println("Received IV: " + Common.bytesToHexString(senderIVBytes));

            // unencrypted challenge from server
            byte[] serverNonce = new byte[Common.NONCE_LENGTH];
            System.arraycopy(responseFromServer, Common.IV_LENGTH, serverNonce, 0, Common.NONCE_LENGTH);
            System.out.println("Received server nonce: " + Common.bytesToHexString(serverNonce));

            byte[] encryptedBytesFromServer = new byte[responseFromServer.length - Common.NONCE_LENGTH - Common.IV_LENGTH];
            System.arraycopy(responseFromServer, Common.IV_LENGTH + Common.NONCE_LENGTH, encryptedBytesFromServer, 0, responseFromServer.length - Common.NONCE_LENGTH - Common.IV_LENGTH);
            BigInteger diffieParam = Common.processDiffieResponse(encryptedBytesFromServer);

            Diffie diffie = new Diffie();

            //Truncates Combined key to suit AES
            byte[] diffieCombinedKey = diffie.getCombinedKey(diffieParam);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashKey = digest.digest(diffieCombinedKey);
            byte[] sessionKey = Arrays.copyOf(hashKey, 16);
            System.out.println("Generated session key: " + Common.bytesToHexString(sessionKey));

            //Sets session key
            Vpn.getVpnManager().setSessionKey(sessionKey);

            BigInteger myDiffieParams = diffie.getPubKey();
            byte[] myDiffieBytes = myDiffieParams.toByteArray();
            System.out.println("Generated my diffie parameters g^a mod p: " + Common.bytesToHexString(myDiffieBytes));

            byte[] responseToServer = Aes.encryptDiffieExchange(serverNonce, Vpn.getVpnManager().getMyIdentity(), myDiffieBytes);
            System.out.println("Encrypted server nonce, literal string CLNT, and my diffie parameters: " + Common.bytesToHexString(responseToServer));

            writer.write(responseToServer);
            System.out.println("Sent encrypted server nonce, encrypted literal string SRVR, and encrypted diffie parameters: " + Common.bytesToHexString(responseToServer));

            Vpn.getVpnManager().setStatus(Status.BothConnected);
            new Thread(new MessageReceiver()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSocket(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
    }
}
