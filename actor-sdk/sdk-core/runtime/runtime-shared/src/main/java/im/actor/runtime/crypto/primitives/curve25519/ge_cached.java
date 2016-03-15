package im.actor.runtime.crypto.primitives.curve25519;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class ge_cached {

    public int[] YplusX;
    public int[] YminusX;
    public int[] Z;
    public int[] T2d;

    public ge_cached() {
        YplusX = new int[10];
        YminusX = new int[10];
        Z = new int[10];
        T2d = new int[10];
    }
}

