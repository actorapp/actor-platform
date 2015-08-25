package im.actor.runtime.markdown;

public class MDSection {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_CODE = 1;

    private int type;
    private MDCode code;
    private MDText[] text;

    public MDSection(MDCode code) {
        this.code = code;
        this.type = TYPE_CODE;
    }

    public MDSection(MDText[] text) {
        this.text = text;
        this.type = TYPE_TEXT;
    }

    public int getType() {
        return type;
    }

    public MDCode getCode() {
        return code;
    }

    public MDText[] getText() {
        return text;
    }
}
