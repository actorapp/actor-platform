package org.bouncycastle.crypto.params;

public class CramerShoupKeyParameters extends AsymmetricKeyParameter {
	
	private CramerShoupParameters params;

	protected CramerShoupKeyParameters(boolean isPrivate, CramerShoupParameters params) {
		super(isPrivate);

		this.params = params;
	}

	public CramerShoupParameters getParameters() {
		return params;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof CramerShoupKeyParameters)) {
			return false;
		}

		CramerShoupKeyParameters csKey = (CramerShoupKeyParameters) obj;

		if (params == null) {
			return csKey.getParameters() == null;
		} else {
			return params.equals(csKey.getParameters());
		}
	}

	public int hashCode() {
		int code = isPrivate() ? 0 : 1;

		if (params != null) {
			code ^= params.hashCode();
		}

		return code;
	}
}
