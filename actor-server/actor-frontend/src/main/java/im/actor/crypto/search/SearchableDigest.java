package im.actor.crypto.search;

import im.actor.crypto.primitives.Digest;
import im.actor.crypto.primitives.digest.SHA256;
import im.actor.crypto.primitives.hmac.HMAC;

public class SearchableDigest {

    // Source: Distinct word length frequencies: distributions and symbol entropies, 2012
    // Reginald Smith, Rochester, NY
    // http://arxiv.org/pdf/1207.2334.pdf;
    private static final int DEFAULT_MAX_LENGTH = 20;

    private int splitValue;
    private Digest digest;
    private SearchableWordDigest searchableWordDigest;
    private byte[] preprocessSecret;
    private byte[] wordKeySecret;
    private HMAC preprocesHmac;
    private HMAC wordKeyHmac;

    public SearchableDigest(int splitValue,
                            Digest digest,
                            byte[] preprocessSecret,
                            byte[] wordKeySecret) {
        this.digest = digest;
        this.splitValue = splitValue;
        this.searchableWordDigest = new SearchableWordDigest(splitValue, digest);
        this.preprocessSecret = preprocessSecret;
        this.wordKeySecret = wordKeySecret;
        this.preprocesHmac = new HMAC(preprocessSecret, digest);
        this.wordKeyHmac = new HMAC(wordKeySecret, digest);
    }

    public SearchableDigest(byte[] preprocessSecret,
                            byte[] wordKeySecret) {
        this(DEFAULT_MAX_LENGTH, new SHA256(), preprocessSecret, wordKeySecret);
    }

    public byte[] digest(String word, byte[] random) {
        return searchableWordDigest.digest(buildWord(word), random);
    }

    public boolean compare(byte[] wordHash, SearchableHashedWord query) {
        return searchableWordDigest.compare(wordHash, query);
    }

    public SearchableHashedWord buildWord(String word) {
        byte[] wordHash = hashWord(word);
        byte[] wordKey = buildWordKey(wordHash);
        return new SearchableHashedWord(wordHash, wordKey);
    }

    private byte[] hashWord(String word) {
        byte[] wordData = word.getBytes();
        preprocesHmac.reset();
        preprocesHmac.update(wordData, 0, wordData.length);
        byte[] key = new byte[digest.getDigestSize()];
        preprocesHmac.doFinal(key, 0);
        return key;
    }

    private byte[] buildWordKey(byte[] wordHash) {
        wordKeyHmac.reset();
        wordKeyHmac.update(wordHash, 0, wordHash.length);
        byte[] key = new byte[digest.getDigestSize()];
        wordKeyHmac.doFinal(key, 0);
        return key;
    }
}