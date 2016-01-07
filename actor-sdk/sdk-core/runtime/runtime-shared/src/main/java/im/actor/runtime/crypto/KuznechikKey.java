package im.actor.runtime.crypto;

public class KuznechikKey {
    private byte[] secretKey;

    public KuznechikKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }
}
