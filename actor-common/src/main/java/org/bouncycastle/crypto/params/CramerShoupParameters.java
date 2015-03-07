package org.bouncycastle.crypto.params;

import java.math.BigInteger;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;

public class CramerShoupParameters implements CipherParameters {

	private BigInteger p; // prime order of G
	private BigInteger g1, g2; // generate G
	
	private Digest H; // hash function

	public CramerShoupParameters(BigInteger p, BigInteger g1, BigInteger g2, Digest H) {
		this.p = p;
		this.g1 = g1;
		this.g2 = g2;
		this.H = H;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof DSAParameters)) {
			return false;
		}

		CramerShoupParameters pm = (CramerShoupParameters) obj;

		return (pm.getP().equals(p) && pm.getG1().equals(g1) && pm.getG2().equals(g2));
	}

	public int hashCode() {
		return getP().hashCode() ^ getG1().hashCode() ^ getG2().hashCode();
	}
	
	public BigInteger getG1() {
		return g1;
	}
	
	public BigInteger getG2() {
		return g2;
	}
	
	public BigInteger getP() {
		return p;
	}
	
	public Digest getH() {
		H.reset();
		return H;
	}
	
}
