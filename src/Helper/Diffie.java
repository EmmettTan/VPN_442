package Helper;

import java.math.BigInteger;
import java.util.Random;

/**
 * Created by hams on 10/10/16.
 * Note: Right now the private key is only generated once, so server should only tak to one client
 *       or the server will be reusing its private key. Work around, in Server.java reinitialize on new connection
 */
public class Diffie {
    private final BigInteger G_PRIME_BASE = new BigInteger("383");

    private final BigInteger P_PRIME_MODULUS = new BigInteger("709");

    private BigInteger Key;

    public Diffie() {
        Random random = new Random();
        int randomNum = (random.nextInt(Integer.MAX_VALUE) % 40);
        randomNum ++;
        Key = BigInteger.valueOf(randomNum);
    }

    public BigInteger calPubKey() {
        return G_PRIME_BASE.modPow(Key, P_PRIME_MODULUS);
    }

    public BigInteger calCombinedKey(Integer receivedKey) {
        BigInteger tempKey = BigInteger.valueOf(receivedKey.intValue());
        return tempKey.modPow(Key, P_PRIME_MODULUS);
    }
}
