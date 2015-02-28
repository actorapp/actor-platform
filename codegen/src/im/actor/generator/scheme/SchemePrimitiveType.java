package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemePrimitiveType extends SchemeType {
    private String name;

    public SchemePrimitiveType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
