package im.actor.model.crypto.encoding;

import java.math.BigInteger;

/**
 * Created by ex3ndr on 10.03.15.
 */
public class PKS8RsaPrivateKey {

    private BigInteger modulus;
    private BigInteger exponent;

    public PKS8RsaPrivateKey(BigInteger modulus, BigInteger exponent) {
        this.modulus = modulus;
        this.exponent = exponent;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getExponent() {
        return exponent;
    }
}
