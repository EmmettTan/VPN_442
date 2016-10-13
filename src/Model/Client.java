package Model;

import Helper.Common;
import Helper.Status;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by karui on 2016-10-03.
 */
public class Client implements Runnable {
    private final static String identity = "CLNT";

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

            byte[] identityBytes = identity.getBytes(Common.ENCODING_TYPE);
            byte[] bytesToSend = new byte[Common.NONCE_LENGTH + Common.IDENTITY_LENGTH];
            System.arraycopy(nonce, 0, bytesToSend, 0, nonce.length);
            System.arraycopy(identityBytes, 0, bytesToSend, nonce.length, identityBytes.length);

            writer.write(bytesToSend);

            // wait for response from server
            while (reader.available() == 0) {}

            byte[] receivedBytes = new byte[reader.available()];
            reader.readFully(receivedBytes);

            // unencrypted challenge from server
            byte[] serverNonce = new byte[Common.NONCE_LENGTH];
            System.arraycopy(receivedBytes, 0, serverNonce, 0, Common.NONCE_LENGTH);

            byte[] myNonceFromServer = new byte[Common.NONCE_LENGTH];
            System.arraycopy(receivedBytes, Common.NONCE_LENGTH, myNonceFromServer, 0, Common.NONCE_LENGTH);
            if (!Arrays.equals(myNonceFromServer, Vpn.getVpnManager().getMyNonce())) {
                System.out.println("TRUDY APPEARED!");
                System.exit(1);
            }

            // TODO: receive identity & diffie params; if everything okay, set status to both connected
            // for now we set the status to both connected directly and assume okay
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

    public static String getIdentity() {
        return identity;
    }
}
