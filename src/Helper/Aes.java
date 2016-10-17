package Helper;

import Model.Vpn;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by karui on 2016/10/12.
 */
public class Aes {
    public static final String CIPHER_SETTINGS = "AES/GCM/NoPadding";

    public static byte[] encrypt(String text, Cipher cipher) {
        try {
            byte[] plaintextBytes = text.getBytes(Common.ENCODING_TYPE);
            return cipher.doFinal(plaintextBytes);
        } catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            Common.handleException(e);
            return null;
        }
    }

    public static String decrypt(byte[] ciphertextBytes, Cipher cipher) {
        try {
            byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);
            return new String(plaintextBytes, Common.ENCODING_TYPE);
        } catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            Common.handleException(e);
            return null;
        }
    }

    public static Cipher getAesCipher(int opmode, SecretKeySpec key) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_SETTINGS);
            GCMParameterSpec gcm = new GCMParameterSpec(128, Vpn.getVpnManager().getIvManager().getIV());
            cipher.init(opmode, key, gcm);
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            Common.handleException(e);
            return null;
        }
    }

    public static byte[] encryptDiffieExchange(byte[] nonce, byte[] identity, byte[] diffie) {
        try {
            byte[] encryptionTarget = new byte[Common.NONCE_LENGTH + Common.IDENTITY_LENGTH + diffie.length];
            System.arraycopy(nonce, 0, encryptionTarget, 0, Common.NONCE_LENGTH);
            System.arraycopy(identity, 0, encryptionTarget, Common.NONCE_LENGTH, Common.IDENTITY_LENGTH);
            System.arraycopy(diffie, 0, encryptionTarget, Common.NONCE_LENGTH + Common.IDENTITY_LENGTH, diffie.length);
            Cipher cipher = getAesCipher(Cipher.ENCRYPT_MODE, Vpn.getVpnManager().getSharedKey());
            return cipher.doFinal(encryptionTarget);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            Common.handleException(e);
            return null;
        }
    }

    public static byte[] decryptDiffieExchange(byte[] encrypted) {
        try {
            Cipher cipher = getAesCipher(Cipher.DECRYPT_MODE, Vpn.getVpnManager().getSharedKey());
            return cipher.doFinal(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            Common.handleException(e);
            return null;
        }
    }
}
