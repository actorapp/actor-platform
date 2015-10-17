package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 22.11.14.
 */
public class SchemeAliasType extends SchemeType {
    private String name;

    public SchemeAliasType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
