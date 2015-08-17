package im.actor.core.util;

public class StringMatch {

    private int start;
    private int length;

    public StringMatch(int start, int length) {
        this.start = start;
        this.length = length;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }
}
