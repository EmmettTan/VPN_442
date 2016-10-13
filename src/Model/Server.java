package Model;

import Helper.Common;
import Helper.Diffie;
import Helper.Status;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by karui on 2016-10-03.
 */
public class Server implements Runnable {
    private final int BUFFER_SIZE = 16;
    private final String identity = "SRVR";

    private ServerSocket serverSocket;
    private Socket clientSocket;

    protected Server() {
        System.out.println("Hi im a server");
    }

    public void bind(int portNum) {
        try {
            serverSocket = new ServerSocket(portNum);
        } catch (IOException e) {
            System.out.println("Failed to bind to port");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void listen() {
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    // TODO: do some diffie hell stuff here; need to send challenge, check nonce from client etc
    @Override
    public void run() {
        try {
            // listen() calls accept(), which is a blocking call
            listen();
            byte[] nonce = Common.setupIdentity(clientSocket);
            DataInputStream reader = Vpn.getVpnManager().getReader();
            DataOutputStream writer = Vpn.getVpnManager().getWriter();

            // order: nonce, identity
            byte[] clientNonce = new byte[Common.NONCE_LENGTH];
            reader.read(clientNonce);
            Vpn.getVpnManager().setClientNonce(clientNonce);
            byte[] clientIdentityBytes = new byte[Common.IDENTITY_LENGTH];
            reader.read(clientIdentityBytes);
            String clientIdentityString = new String(clientIdentityBytes, Common.ENCODING_TYPE);
            if (!clientIdentityString.equals(Client.getIdentity())) {
                System.out.println("FAKE CLIENT");
                System.exit(1);
            }
//            byte[] remaining = new byte[reader.available()];
//            reader.readFully(remaining);

            // order: server nonce, encrypted (client nonce, identity, diffie params)
            Diffie diffie = new Diffie();
            BigInteger diffieParams = diffie.calPubKey();
            byte[] identityBytes = identity.getBytes(Common.ENCODING_TYPE);
            byte[] diffieBytes = diffieParams.toByteArray();
            byte[] encryptionTarget = new byte[Common.NONCE_LENGTH + Common.IDENTITY_LENGTH + diffieBytes.length];

            // put bytes into encryption target; TODO encrypt with shared key
            System.arraycopy(clientNonce, 0, encryptionTarget, 0, Common.NONCE_LENGTH);
            System.arraycopy(identityBytes, 0, encryptionTarget, Common.NONCE_LENGTH, Common.IDENTITY_LENGTH);
            System.arraycopy(diffieBytes, 0, encryptionTarget, Common.NONCE_LENGTH + Common.IDENTITY_LENGTH, diffieBytes.length);

            byte[] bytesToSend = new byte[Common.NONCE_LENGTH + encryptionTarget.length];
            System.arraycopy(nonce, 0, bytesToSend, 0, nonce.length);
            System.arraycopy(encryptionTarget, 0, bytesToSend, nonce.length, encryptionTarget.length);

            writer.write(bytesToSend);

            while (reader.available() == 0) {}

            // TODO: check nonce here; if okay, set status to both connected; for now we just assume it's okay
            Vpn.getVpnManager().setStatus(Status.BothConnected);
            new Thread(new MessageReceiver()).start();
        } catch (Exception e) {
            Vpn.getVpnManager().terminate();
            e.printStackTrace();
        }
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
}
