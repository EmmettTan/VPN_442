package Model;

import Helper.Common;
import Helper.Status;
import Helper.Diffie;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.math.BigInteger;

/**
 * Created by karui on 2016-10-03.
 */
public class Server implements Runnable {
    private final int BUFFER_SIZE = 16;

    private ServerSocket serverSocket;
    private Socket clientSocket;

    protected Server() {
        System.out.println("Hi im a server");
    }

    public boolean bind(int portNum) {
        try {
            serverSocket = new ServerSocket(portNum);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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

            //Do the Diffie
            Diffie serverDiffie = new Diffie();
            int clientKey = reader.readInt();
            writer.writeInt(serverDiffie.calPubKey().intValue());
            BigInteger combinedKey = serverDiffie.calCombinedKey(clientKey);
            //writer.write(nonce);

            // client nonce
            //byte[] clientNonce = new byte[Common.NONCE_LENGTH];
            //reader.read(clientNonce);
            //System.out.println(clientNonce);
            //Vpn.getVpnManager().setClientNonce(clientNonce);

            //byte[] buffer = new byte[BUFFER_SIZE];
            //byte[] ciphertext = new byte[reader.read(buffer)];
            //System.arraycopy(buffer, 0, ciphertext, 0, ciphertext.length);

            // TODO: check nonce here; if okay, set status to both connected; for now we just assume it's okay
            Vpn.getVpnManager().setStatus(Status.BothConnected);
            new Thread(new MessageReceiver()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
}
