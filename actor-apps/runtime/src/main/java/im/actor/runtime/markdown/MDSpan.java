package im.actor.runtime.markdown;

public class MDSpan extends MDText {

    public static final int TYPE_BOLD = 0;
    public static final int TYPE_ITALIC = 1;
    public static final int TYPE_URL = 2;

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

    @Override
    public String toMarkdown() {
        String res = "";
        if (spanType == TYPE_BOLD) {
            res += "*";
        } else if (spanType == TYPE_ITALIC) {
            res += "_";
        } if (spanType == TYPE_URL) {

        } else {
            throw new RuntimeException("Unknown type");
        }

        for (MDText t : child) {
            res += t.toMarkdown();
        }

        if (spanType == TYPE_BOLD) {
            res += "*";
        } else if (spanType == TYPE_ITALIC) {
            res += "_";
        } else {
            throw new RuntimeException("Unknown type");
        }
        return res;
    }
}
