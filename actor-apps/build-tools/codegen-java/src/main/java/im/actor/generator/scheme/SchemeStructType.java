package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeStructType extends SchemeType {
    private String type;

    public SchemeStructType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
