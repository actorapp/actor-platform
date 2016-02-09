package im.actor.crypto.search;

import im.actor.crypto.primitives.Digest;
import im.actor.crypto.primitives.digest.SHA256;
import im.actor.crypto.primitives.hmac.HMAC;

public class SearchableWordDigest {

    public static SearchableWordDigest DEFAULT() {
        return new SearchableWordDigest(20, new SHA256());
    }

    private int splitValue;
    private Digest baseDigest;

    public SearchableWordDigest(int splitValue, Digest baseDigest) {
        if (splitValue > baseDigest.getDigestSize() - 8) {
            throw new IllegalArgumentException("Split value can't be bigger than digest size - 6 ");
        }
        this.splitValue = splitValue;
        this.baseDigest = baseDigest;
    }

    public byte[] digest(SearchableHashedWord word, byte[] random) {
        if (word.getWord().length != baseDigest.getDigestSize()) {
            throw new IllegalArgumentException("Word length MUST be equal to digest size");
        }
        if (random.length < splitValue) {
            throw new IllegalArgumentException("random length MUST be >= split value");
        }

        byte[] res = new byte[baseDigest.getDigestSize()];
        for (int i = 0; i < splitValue; i++) {
            res[i] = random[i];
        }

        HMAC hmac = new HMAC(word.getWordKey(), baseDigest);
        hmac.reset();
        hmac.update(res, 0, splitValue);
        byte[] dest = new byte[baseDigest.getDigestSize()];
        hmac.doFinal(dest, 0);
        for (int i = 0; i < baseDigest.getDigestSize() - splitValue; i++) {
            res[i + splitValue] = dest[i];
        }
        for (int i = 0; i < baseDigest.getDigestSize(); i++) {
            res[i] = (byte) (res[i] ^ word.getWord()[i]);
        }
        return res;
    }

    public boolean compare(byte[] digest, SearchableHashedWord queryWord) {
        if (queryWord.getWord().length != baseDigest.getDigestSize()) {
            throw new IllegalArgumentException("Word length MUST be equal to digest size");
        }
        byte[] tmp = new byte[baseDigest.getDigestSize()];
        for (int i = 0; i < baseDigest.getDigestSize(); i++) {
            tmp[i] = (byte) (digest[i] ^ queryWord.getWord()[i]);
        }

        HMAC hmac = new HMAC(queryWord.getWordKey(), baseDigest);
        hmac.update(tmp, 0, splitValue);
        byte[] dest = new byte[baseDigest.getDigestSize()];
        hmac.doFinal(dest, 0);
        for (int i = 0; i < baseDigest.getDigestSize() - splitValue; i++) {
            if (dest[i] != tmp[i + splitValue]) {
                return false;
            }
        }
        return true;
    }
}