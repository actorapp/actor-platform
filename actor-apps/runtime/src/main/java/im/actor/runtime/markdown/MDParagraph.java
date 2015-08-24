package im.actor.runtime.markdown;

public class MDParagraph {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_CODE = 1;

    private int type;
    private String text;

    public MDParagraph(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}