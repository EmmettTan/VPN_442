package Helper;

import java.math.BigInteger;
import java.security.*;

import java.security.spec.InvalidKeySpecException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.KeyAgreement;

/**
 * Created by hams on 10/10/16.
 */
public class Diffie {

    private final BigInteger G_PRIME_BASE = new BigInteger("25780203759537142844476139018864480702740908341959346311345598211614600030091611862405021278192612386456629876791968308041681997122678698558000967593535797368021295319652065273702113226635002653154004149562584334212130425969213269861262215267375922448433273681046697536269845106842950188189152025787051874541257772012909272093373769879832457236505292258299009056092204695541137472436978752451126939590746052478787889318938014799286995951225673696614138703080064144116853207922017484772963953594596359475616306396572286304704947349022569339416139265898374628180097542443950494168371746770710050366082670536185473955637");

    private final BigInteger P_PRIME_MODULUS = new BigInteger("18820600821012328706771116077499038278823329410855091633359767786545221415814150459839150158939010456768875105709653433208156416225423623336164959211282768104961342340049613319601186966137334252164602652394215919618605464671480913611185227440810812673350753931131848970808735640058703330444583392137642804973481483216995387607185192767831721481456495255691273497749432534923082043171427781688955549834801947590617552040013918149843860320396579090508984661438711833622296521027250435322709944731285477619653768797368138516562762571100196776929702093975568467051789522970342249778666431177430817074596940537214429161587");

    private KeyPair keyPair;

    public Diffie() {
        try {
            //Making DH Generator with P and G
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DiffieHellman");
            DHParameterSpec DHParam = new DHParameterSpec(P_PRIME_MODULUS, G_PRIME_BASE);
            keyPairGen.initialize(DHParam);
            //Create Key Pair
            keyPair = keyPairGen.generateKeyPair();
        } catch (InvalidAlgorithmParameterException b) {
            b.printStackTrace();
            System.out.println("Invalid Algorithm Parameter Exception");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("Diffie no such algorithm exception");
        }
    }

    public BigInteger getPubKey() {
        try {
            KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");
            DHPublicKeySpec kspec = kfactory.getKeySpec(keyPair.getPublic(),
                    DHPublicKeySpec.class);
            return kspec.getY();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getCombinedKey(BigInteger receivedKey) {
        try {
            //Convert BigInt to class of DHKey
            KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");
            DHPublicKeySpec receivedKeySpec = new DHPublicKeySpec( receivedKey, P_PRIME_MODULUS, G_PRIME_BASE);
            DHPublicKey DHPubKey = (DHPublicKey) kfactory.generatePublic(receivedKeySpec);

            //Generates Shared key
            KeyAgreement keyAgree = KeyAgreement.getInstance("DiffieHellman");
            keyAgree.init(keyPair.getPrivate());
            keyAgree.doPhase(DHPubKey, true);

            //Returns Shared Key
            byte[] resultKey = keyAgree.generateSecret();
            return resultKey;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
