package im.actor.messenger.app.util;

public class VisibleViewItem {
    private int index;
    private int top;
    private long id;

    public VisibleViewItem(int index, int top, long id) {
        this.index = index;
        this.top = top;
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public int getTop() {
        return top;
    }

    public long getId() {
        return id;
    }
}