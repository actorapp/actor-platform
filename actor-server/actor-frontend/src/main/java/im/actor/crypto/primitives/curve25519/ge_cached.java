
package im.actor.crypto.primitives.curve25519;

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

