package Helper;

import Model.Vpn;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by karui on 2016-10-03.
 */
public class Common {
    public static final int NONCE_LENGTH = 4;

    public static final String ENCODING_TYPE = "UTF-8";

    public static final int BLOCK_SIZE = 16;

    public static byte[] setupIdentity(Socket clientSocket) {
        try {
            DataInputStream reader = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
            Vpn.getVpnManager().setReader(reader);
            Vpn.getVpnManager().setWriter(writer);
            Vpn.getVpnManager().setNonce();
            Vpn.getVpnManager().initIvManager();

            // send the nonce; should we send it as a string? i.e. writer.writeUTF(nonce.toString());
            return Vpn.getVpnManager().getMyNonce();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: check for null in upper level
            return null;
        }
    }

    public static byte[] setCorrectBlockLength(byte[] a) {
        if (a.length % Common.BLOCK_SIZE != 0) {
            int diff = Common.BLOCK_SIZE - (a.length % Common.BLOCK_SIZE);
            a = Arrays.copyOf(a, a.length + diff);
        }
        return a;
    }
}
