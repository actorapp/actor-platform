package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ge_p2 {

    public int[] X;
    public int[] Y;
    public int[] Z;

    public ge_p2() {
        X = new int[10];
        Y = new int[10];
        Z = new int[10];
    }
}

