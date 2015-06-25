package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class SchemeEnumType extends SchemeType {
    private String name;

    public SchemeEnumType(String name) {
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
