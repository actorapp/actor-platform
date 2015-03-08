package org.bouncycastle.crypto.params;

import java.math.BigInteger;

public class CramerShoupPrivateKeyParameters extends CramerShoupKeyParameters {
	
	private BigInteger x1, x2, y1, y2, z; // Z_p
	private CramerShoupPublicKeyParameters pk; // public key

	public CramerShoupPrivateKeyParameters(CramerShoupParameters params, BigInteger x1, BigInteger x2, BigInteger y1, BigInteger y2, BigInteger z) {
		super(true, params);

		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.z = z;
	}

	public BigInteger getX1() {
		return x1;
	}
	
	public BigInteger getX2() {
		return x2;
	}
	
	public BigInteger getY1() {
		return y1;
	}
	
	public BigInteger getY2() {
		return y2;
	}
	
	public BigInteger getZ() {
		return z;
	}
	
	public void setPk(CramerShoupPublicKeyParameters pk) {
		this.pk = pk;
	}
	
	public CramerShoupPublicKeyParameters getPk() {
		return pk;
	}

	public int hashCode() {
		return x1.hashCode() ^ x2.hashCode() ^ y1.hashCode() ^ y2.hashCode() ^ z.hashCode() ^ super.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof CramerShoupPrivateKeyParameters)) {
			return false;
		}

		CramerShoupPrivateKeyParameters other = (CramerShoupPrivateKeyParameters) obj;

		return other.getX1().equals(this.x1) && other.getX2().equals(this.x2) && other.getY1().equals(this.y1) && other.getY2().equals(this.y2) && other.getZ().equals(this.z) && super.equals(obj);
	}
}
