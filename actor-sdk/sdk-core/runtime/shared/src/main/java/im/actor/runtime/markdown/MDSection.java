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

    public String toMarkdown() {
        if (type == TYPE_TEXT) {
            String res = "";
            for (MDText t : text) {
                res += t.toMarkdown();
            }
            return res;
        } else if (type == TYPE_CODE) {
            return "```\n" +
                    code.getCode() +
                    "\n```";
        } else {
            throw new RuntimeException("Unknown type");
        }
    }
}
