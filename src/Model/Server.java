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

/**
 * Created by karui on 2016-10-03.
 */
public class Server implements Runnable {
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
            Common.validateByteEquality(clientIdentityBytes, Vpn.getVpnManager().getOppositeIdentity());

            // order: server nonce, encrypted (client nonce, identity, diffie params); move this to another method after DH is done
            Diffie diffie = new Diffie();
            BigInteger myDiffieParams = diffie.calPubKey();
            byte[] myDiffieBytes = myDiffieParams.toByteArray();
            byte[] encryptionTarget = Common.encryptDiffieExchange(clientNonce, Vpn.getVpnManager().getMyIdentity(), myDiffieBytes);

            byte[] bytesToSend = new byte[Common.NONCE_LENGTH + encryptionTarget.length];
            System.arraycopy(nonce, 0, bytesToSend, 0, nonce.length);
            System.arraycopy(encryptionTarget, 0, bytesToSend, nonce.length, encryptionTarget.length);

            writer.write(bytesToSend);

            while (reader.available() == 0) {}

            byte[] responseFromClient = new byte[reader.available()];
            reader.readFully(responseFromClient);
            BigInteger diffieParam = Common.processDiffieResponse(responseFromClient);
            // TODO compute diffie key

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
