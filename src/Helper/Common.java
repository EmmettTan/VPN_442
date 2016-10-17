package Helper;

import Model.Vpn;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by karui on 2016-10-03.
 */
public class Common {
    public static final int NONCE_LENGTH = 4;

    public static final int IDENTITY_LENGTH = 4;

    public static final String ENCODING_TYPE = "UTF-8";

    public static final int IV_LENGTH = 16;

    public static final String GENERIC_ERROR_MESSAGE = "An unexpected error has occurred: ";

    public static byte[] setupIdentity(Socket clientSocket) {
        try {
            DataInputStream reader = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
            Vpn.getVpnManager().setReader(reader);
            Vpn.getVpnManager().setWriter(writer);
            Vpn.getVpnManager().setNonce();
            Vpn.getVpnManager().initIvManager();
            return Vpn.getVpnManager().getMyNonce();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(GENERIC_ERROR_MESSAGE + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    public static BigInteger processDiffieResponse(byte[] encryptedBytes) {
        byte[] decrypted = Aes.decryptDiffieExchange(encryptedBytes);

        // check my nonce
        byte[] myNonceFromResponse = new byte[Common.NONCE_LENGTH];
        System.arraycopy(decrypted, 0, myNonceFromResponse, 0, Common.NONCE_LENGTH);
        Common.validateByteEquality(myNonceFromResponse, Vpn.getVpnManager().getMyNonce());

        // check identity
        byte[] serverIdentityBytes = new byte[Common.IDENTITY_LENGTH];
        System.arraycopy(decrypted, Common.NONCE_LENGTH, serverIdentityBytes, 0, Common.IDENTITY_LENGTH);
        Common.validateByteEquality(serverIdentityBytes, Vpn.getVpnManager().getOppositeIdentity());

        // compute DH
        int startOfDiffieBytes = Common.NONCE_LENGTH + Common.IDENTITY_LENGTH;
        int diffieBytesFromServerLen = decrypted.length - startOfDiffieBytes;
        byte[] diffieBytesFromServer = new byte[diffieBytesFromServerLen];
        System.arraycopy(decrypted, startOfDiffieBytes, diffieBytesFromServer, 0, diffieBytesFromServerLen);
        BigInteger diffieInt = new BigInteger(diffieBytesFromServer);
        return diffieInt;
    }

    public static void validateByteEquality(byte[] actual, byte[] expected) {
        if (!Arrays.equals(actual, expected)) {
            System.out.println("TRUDY APPEARED! OMG!!!");
            Vpn.getVpnManager().terminate();
            System.exit(1);
        }
    }

    public static void handleException(Exception e) {
        Vpn.getVpnManager().terminate();
        //e.printStackTrace();
        System.err.println(GENERIC_ERROR_MESSAGE + e.getMessage());
        System.exit(1);
    }
}
