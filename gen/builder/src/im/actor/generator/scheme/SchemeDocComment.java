package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 18.11.14.
 */
public class SchemeDocComment extends SchemeDoc {
    private String text;

    public SchemeDocComment(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
