package im.actor.runtime.generic.mvvm.alg;

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
