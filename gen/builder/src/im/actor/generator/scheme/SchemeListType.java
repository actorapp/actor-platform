package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeListType extends SchemeType {
    private SchemeType type;

    public SchemeListType(SchemeType type) {
        this.type = type;
    }

    public SchemeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "list<" + type + ">";
    }
}
