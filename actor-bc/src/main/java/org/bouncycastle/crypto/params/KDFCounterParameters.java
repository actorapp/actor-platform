package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.DerivationParameters;
import org.bouncycastle.util.Arrays;

/**
 * This KDF has been defined by the publicly available NIST SP 800-108 specification.
 * NIST SP800-108 allows for alternative orderings of the input fields, meaning that the input can be formated in multiple ways.
 * There are 3 supported formats:  - Below [i]_2 is a counter of r-bits length concatenated to the fixedInputData.
 * <ul>
 * <li>1: K(i) := PRF( KI, [i]_2 || Label || 0x00 || Context || [L]_2 ) with the counter at the very beginning of the fixedInputData (The default implementation has this format)</li>
 * <li>2: K(i) := PRF( KI, Label || 0x00 || Context || [L]_2 || [i]_2 ) with the counter at the very end of the fixedInputData</li>
 * <li>3a: K(i) := PRF( KI, Label || 0x00 || [i]_2 || Context || [L]_2 ) OR:</li>
 * <li>3b: K(i) := PRF( KI, Label || 0x00 || [i]_2 || [L]_2 || Context ) OR:</li>
 * <li>3c: K(i) := PRF( KI, Label || [i]_2 || 0x00 || Context || [L]_2 ) etc... with the counter somewhere in the 'middle' of the fixedInputData.</li>
 * </ul>
 * <p>
 * This function must be called with the following KDFCounterParameters():
 *  - KI     <br/>
 *  - The part of the fixedInputData that comes BEFORE the counter OR null  <br/>
 *  - the part of the fixedInputData that comes AFTER the counter OR null  <br/>
 *  - the length of the counter in bits (not bytes) <br/>
 * </p>
 * Resulting function calls assuming an 8 bit counter.
 * <ul>
 * <li>1.  KDFCounterParameters(ki, 	null, 									"Label || 0x00 || Context || [L]_2]",	8); </li>
 * <li>2.  KDFCounterParameters(ki, 	"Label || 0x00 || Context || [L]_2]", 	null,									8); </li>
 * <li>3a. KDFCounterParameters(ki, 	"Label || 0x00",						"Context || [L]_2]",					8);  </li>
 * <li>3b. KDFCounterParameters(ki, 	"Label || 0x00",						"[L]_2] || Context",					8);</li>
 * <li>3c. KDFCounterParameters(ki, 	"Label", 								"0x00 || Context || [L]_2]",			8); </li>
 * </ul>
 */
public final class KDFCounterParameters
    implements DerivationParameters
{

    private byte[] ki;
    private byte[] fixedInputDataCounterPrefix;
    private byte[] fixedInputDataCounterSuffix;
    private int r;

    /**
     * Base constructor - suffix fixed input data only.
     *
     * @param ki the KDF seed
     * @param fixedInputDataCounterSuffix  fixed input data to follow counter.
     * @param r length of the counter in bits.
     */
    public KDFCounterParameters(byte[] ki, byte[] fixedInputDataCounterSuffix, int r)
    {
    	this(ki, null, fixedInputDataCounterSuffix, r);
    }

    /**
     * Base constructor - prefix and suffix fixed input data.
     *
     * @param ki the KDF seed
     * @param fixedInputDataCounterPrefix fixed input data to precede counter
     * @param fixedInputDataCounterSuffix fixed input data to follow counter.
     * @param r length of the counter in bits.
     */
    public KDFCounterParameters(byte[] ki, byte[] fixedInputDataCounterPrefix, byte[] fixedInputDataCounterSuffix, int r)
    {
        if (ki == null)
        {
            throw new IllegalArgumentException("A KDF requires Ki (a seed) as input");
        }
        this.ki = Arrays.clone(ki);

        if (fixedInputDataCounterPrefix == null)
        {
            this.fixedInputDataCounterPrefix = new byte[0];
        }
        else
        {
            this.fixedInputDataCounterPrefix = Arrays.clone(fixedInputDataCounterPrefix);
        }
        
        if (fixedInputDataCounterSuffix == null)
        {
            this.fixedInputDataCounterSuffix = new byte[0];
        }
        else
        {
            this.fixedInputDataCounterSuffix = Arrays.clone(fixedInputDataCounterSuffix);
        }

        if (r != 8 && r != 16 && r != 24 && r != 32)
        {
            throw new IllegalArgumentException("Length of counter should be 8, 16, 24 or 32");
        }
        this.r = r;
    }
    
    public byte[] getKI()
    {
        return ki;
    }

    public byte[] getFixedInputData()
    {
    	//Retained for backwards compatibility
        return Arrays.clone(fixedInputDataCounterSuffix);
    }

    public byte[] getFixedInputDataCounterPrefix()
    {
        return Arrays.clone(fixedInputDataCounterPrefix);
    }
    
    public byte[] getFixedInputDataCounterSuffix()
    {
        return Arrays.clone(fixedInputDataCounterSuffix);
    }

    public int getR()
    {
        return r;
    }
}
