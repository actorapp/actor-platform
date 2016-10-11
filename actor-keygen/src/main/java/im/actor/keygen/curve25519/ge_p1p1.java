package im.actor.keygen.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ge_p1p1 {

    public int[] X;
    public int[] Y;
    public int[] Z;
    public int[] T;

    public ge_p1p1() {
        X = new int[10];
        Y = new int[10];
        Z = new int[10];
        T = new int[10];
    }
}

