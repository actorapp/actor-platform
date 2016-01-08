package im.actor.crypto.primitives.curve25519;

public interface Sha512 {

  public void calculateDigest(byte[] out, byte[] in, long length);

}
