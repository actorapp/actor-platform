package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 17.11.14.
 */
public class SchemeComment implements SchemeRecord {
    private String text;

    public SchemeComment(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
