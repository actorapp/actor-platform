package im.actor.crypto.search;

public class SearchableHashedWord {

    private byte[] word;
    private byte[] wordKey;

    public SearchableHashedWord(byte[] word, byte[] wordKey) {
        if (word.length != wordKey.length) {
            throw new IllegalArgumentException("word ad wordKey length MUST be equal");
        }
        this.word = word;
        this.wordKey = wordKey;
    }

    public byte[] getWord() {
        return word;
    }

    public byte[] getWordKey() {
        return wordKey;
    }
}
