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
            System.arraycopy(identityBytes, 0, bytesToSend, nonce.length, identityBytes.length);

            writer.write(bytesToSend);

            // wait for response from server
            while (reader.available() == 0) {}

            byte[] responseFromServer = new byte[reader.available()];
            reader.readFully(responseFromServer);

            // unencrypted challenge from server
            byte[] serverNonce = new byte[Common.NONCE_LENGTH];
            System.arraycopy(responseFromServer, 0, serverNonce, 0, Common.NONCE_LENGTH);

            byte[] encryptedBytesFromServer = new byte[responseFromServer.length - Common.NONCE_LENGTH];
            System.arraycopy(responseFromServer, Common.NONCE_LENGTH, encryptedBytesFromServer, 0, responseFromServer.length - Common.NONCE_LENGTH);
            BigInteger diffieParam = Common.processDiffieResponse(encryptedBytesFromServer);
            // TODO compute diffie key

            // TOOD filler code for calculating g^a mod p to send to server; remove after we have DH code
            Diffie diffie = new Diffie();
            BigInteger myDiffieParams = diffie.calPubKey();
            byte[] myDiffieBytes = myDiffieParams.toByteArray();

            byte[] responseToServer = Aes.encryptDiffieExchange(serverNonce, Vpn.getVpnManager().getMyIdentity(), myDiffieBytes);
            writer.write(responseToServer);

            Vpn.getVpnManager().setStatus(Status.BothConnected);
            new Thread(new MessageReceiver()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSocket(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
        } catch (IOException e) {
            System.out.println("Failed to connect to server");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
