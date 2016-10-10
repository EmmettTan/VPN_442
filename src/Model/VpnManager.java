package Model;

import Helper.Common;
import Helper.Status;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Created by karui on 2016-10-03.
 */
public class VpnManager {
    private final int SHARED_KEY_LENGTH = 16;
    private final String CIPHER_TYPE = "AES";

    private DataInputStream reader;
    private DataOutputStream writer;
    private Status status;
    private Server server;
    private Client client;
    private int port;
    private byte[] myNonce;
    private byte[] clientNonce;
    private String ip;
    private SecretKeySpec aesKey;

    private VpnManager() {
        reader = null;
        writer = null;
        status = Status.Disconnected;
        server = null;
        client = null;
        port = -1;
        myNonce = null;
        clientNonce = null;
        ip = "";
    }

    public static VpnManager getInstance() {
        if (Vpn.getVpnManager() == null) {
            return new VpnManager();
        }
        return Vpn.getVpnManager();
    }


    public void receiveIp(String ip) {
        this.ip = ip;
    }

    public void receivePort(int port) {
        // TODO: check for invalid port
        this.port = port;
    }

    public void receiveSecret(String secret) {
        // TODO: check key length? or do the arrays copy thing to force key to always be 16 bytes?
        try {
            aesKey = new SecretKeySpec(secret.getBytes(Common.ENCODING_TYPE), CIPHER_TYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void initializeServer() {
        try {
            server = new Server();

            if (server.bind(port)) {
                status = Status.SeverConnected;
                System.out.println("Magic is happening on port " + port);
            } else {
                System.err.println("can't connect to port; DO SOMETHING ABOUT IT; terminate application and yell at human?");
                throw new Exception(); // TODO: throw an actual exception not generic exception
            }

            new Thread(server).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeClient() {
        client = new Client();
        if (client.setSocket(ip, port)) {
            status = Status.ClientConnected;
            new Thread(client).start();
        } else {
            System.out.println("connection refused; tell user");
        }
    }

    public void terminate() {
        try {
            reader.close();
            writer.close();
            if (server != null) {
                server.getServerSocket().close();
                server.getClientSocket().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNonce() {
        SecureRandom sr = new SecureRandom();
        myNonce = new byte[Common.NONCE_LENGTH];
        sr.nextBytes(myNonce);
    }

    public void setReader(DataInputStream reader) {
        this.reader = reader;
    }

    public void setWriter(DataOutputStream writer) {
        this.writer = writer;
    }

    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setServer(Server s) {
        server = s;
    }

    public void setClient(Client c) {
        client = c;
    }

    public byte[] getMyNonce() {
        return myNonce;
    }

    public DataInputStream getReader() {
        return reader;
    }

    public Status getStatus() {
        return status;
    }

    public DataOutputStream getWriter() {
        return writer;
    }

    public ServerSocket getServerSocket() {
        return server.getServerSocket();
    }

    public SecretKeySpec getAesKey() {
        return aesKey;
    }
}
