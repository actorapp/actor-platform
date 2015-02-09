package im.actor.model.entity.content;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class TextContent extends AbsContent {
    private final String text;

    public TextContent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
