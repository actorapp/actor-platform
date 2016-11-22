package im.actor.runtime.markdown;

public class MDRawText extends MDText {
    private String rawText;

    public MDRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getRawText() {
        return rawText;
    }

    @Override
    public String toMarkdown() {
        return rawText;
    }
}
