package Helper;

import java.security.SecureRandom;

/**
 * Created by Emmett on 10/10/2016.
 */
public class IVManager {
    public static final int IV_LENGTH = 16;
    private static byte[] IV = new byte[IV_LENGTH];

    public IVManager() {
        refreshIV();
    }

    public void refreshIV() {
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(IV);
    }

    /* Used to sync the receiver's IV to the sender's IV */
    public void setIV(byte[] iv) {
        this.IV = iv;
    }

    public byte[] getIV() {
        return IV;
    }
}
