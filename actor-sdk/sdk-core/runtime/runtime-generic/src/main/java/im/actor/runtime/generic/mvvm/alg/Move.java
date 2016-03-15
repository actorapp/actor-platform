package im.actor.runtime.generic.mvvm.alg;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

public class Move {
    private int sourceIndex;
    private int destIndex;

    public Move(int sourceIndex, int destIndex) {
        this.sourceIndex = sourceIndex;
        this.destIndex = destIndex;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public int getDestIndex() {
        return destIndex;
    }
}
