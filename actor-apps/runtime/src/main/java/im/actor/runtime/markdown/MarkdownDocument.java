package im.actor.runtime.markdown;

public class MarkdownDocument {

    private MarkdownElement rootElement;

    public MarkdownDocument(MarkdownElement rootElement) {
        this.rootElement = rootElement;
    }

    public MarkdownElement getRootElement() {
        return rootElement;
    }
}