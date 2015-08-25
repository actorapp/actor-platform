package im.actor.runtime.markdown;

public class MDSpan extends MDText {

    public static final int TYPE_BOLD = 0;
    public static final int TYPE_ITALIC = 1;

    private int spanType;
    private MDText[] child;

    public MDSpan(int spanType, MDText[] child) {
        this.spanType = spanType;
        this.child = child;
    }

    public int getSpanType() {
        return spanType;
    }

    public MDText[] getChild() {
        return child;
    }
}
