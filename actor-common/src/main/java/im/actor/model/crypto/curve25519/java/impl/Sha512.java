package im.actor.model.crypto.curve25519.java.impl;

public interface Sha512 {

  public void calculateDigest(byte[] out, byte[] in, long length);

}
