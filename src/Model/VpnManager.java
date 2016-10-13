package Model;

import Helper.Common;
import Helper.Status;
import Helper.IVManager;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.security.SecureRandom;

/**
 * Created by karui on 2016-10-03.
 */
public class VpnManager {
    private final int SHARED_KEY_LENGTH = 16;
    private final String CIPHER_TYPE = "AES";
    private final String SERVER_IDENTITY = "SRVR";
    private final String CLIENT_IDENTITY = "CLNT";

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
    private IVManager ivManager;
    private SecretKeySpec sessionKey;

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
            // TODO REMOVE THIS AFTER DIFFIE
            setSessionKey("1234567890123456");
            server.bind(port);
            status = Status.SeverConnected;
            System.out.println("Magic is happening on port " + port);
            new Thread(server).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeClient() {
        client = new Client();
        client.setSocket(ip, port);
        status = Status.ClientConnected;
        setSessionKey("1234567890123456");
        new Thread(client).start();
    }

    public byte[] getMyIdentity() {
        try {
            return server == null ? CLIENT_IDENTITY.getBytes(Common.ENCODING_TYPE) : SERVER_IDENTITY.getBytes(Common.ENCODING_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An unexpected error has occurred. Aborting");
            System.exit(1);
            return null;
        }
    }

    public byte[] getOppositeIdentity() {
        try {
            return server == null ? SERVER_IDENTITY.getBytes(Common.ENCODING_TYPE) : CLIENT_IDENTITY.getBytes(Common.ENCODING_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An unexpected error has occurred. Aborting");
            System.exit(1);
            return null;
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

    public void setSessionKey(String key) {
        try {
            sessionKey = new SecretKeySpec(key.getBytes(Common.ENCODING_TYPE), CIPHER_TYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void initIvManager() { ivManager = new IVManager(); }

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

    public SecretKeySpec getSessionKey() {
        return sessionKey;
    }

    public IVManager getIvManager() {
        return ivManager;
    }
}
