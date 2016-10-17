package Model;

import Helper.Aes;
import Helper.Common;
import Helper.Diffie;
import Helper.Status;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

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
           Common.handleException(e);
        }
    }

    public void listen() {
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            Common.handleException(e);
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

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

            // order: server nonce, encrypted (client nonce, identity, diffie params); TODO move this to another method after DH is done
            Diffie diffie = new Diffie();
            BigInteger myDiffieParams = diffie.getPubKey();
            byte[] myDiffieBytes = myDiffieParams.toByteArray();
            byte[] ivByteArray = Vpn.getVpnManager().getIvManager().getIV();
            byte[] encryptionTarget = Aes.encryptDiffieExchange(clientNonce, Vpn.getVpnManager().getMyIdentity(), myDiffieBytes);

            byte[] bytesToSend = new byte[ivByteArray.length + Common.NONCE_LENGTH + encryptionTarget.length];
            System.arraycopy(ivByteArray, 0, bytesToSend, 0, ivByteArray.length);
            System.arraycopy(nonce, 0, bytesToSend, ivByteArray.length, nonce.length);
            System.arraycopy(encryptionTarget, 0, bytesToSend, ivByteArray.length + nonce.length, encryptionTarget.length);

            writer.write(bytesToSend);

            while (reader.available() == 0) {}

            byte[] responseFromClient = new byte[reader.available()];
            reader.readFully(responseFromClient);
            BigInteger diffieParam = Common.processDiffieResponse(responseFromClient);

            //Truncate Combined key to suit AES
            byte[] sharedKey = diffie.getCombinedKey(diffieParam);
            byte[] sessionKey = Arrays.copyOf(sharedKey, 16);

            //Set Session key
            Vpn.getVpnManager().setSessionKey(sessionKey);

            Vpn.getVpnManager().setStatus(Status.BothConnected);
            new Thread(new MessageReceiver()).start();
        } catch (Exception e) {
            Vpn.getVpnManager().terminate();
            Common.handleException(e);
        }
    }
}
