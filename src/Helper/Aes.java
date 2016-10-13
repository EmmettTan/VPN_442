package Helper;

import Model.Vpn;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by karui on 2016/10/12.
 */
public class Aes {
    public static final String CIPHER_SETTINGS = "AES/GCM/NoPadding";

    public static Cipher getAesCipher(int opmode, SecretKeySpec key) {
        try {

            Cipher cipher = Cipher.getInstance(CIPHER_SETTINGS);
            GCMParameterSpec gcm = new GCMParameterSpec(128, Vpn.getVpnManager().getIvManager().getIV());
            cipher.init(opmode, key, gcm);
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            // TODO: DEAL WITH THIS
            e.printStackTrace();
            return null;
        }
    }
}
