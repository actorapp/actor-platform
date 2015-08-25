package im.actor.runtime.markdown;

public class MarkdownElement {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_BOLD = 2;
    public static final int TYPE_ITALIC = 3;

    private int type;
    private String text;
    private MarkdownElement[] child;

    public MarkdownElement(int type, String text, MarkdownElement[] child) {
        this.type = type;
        this.text = text;
        this.child = child;
    }

    public MarkdownElement(int type, String text) {
        this.type = type;
        this.text = text;
        this.child = new MarkdownElement[0];
    }

    public MarkdownElement(String text) {
        this.type = TYPE_TEXT;
        this.text = text;
        this.child = new MarkdownElement[0];
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public MarkdownElement[] getChild() {
        return child;
    }
}
