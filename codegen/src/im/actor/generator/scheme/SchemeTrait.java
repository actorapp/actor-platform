package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 22.11.14.
 */
public class SchemeTrait implements SchemeRecord {
    private String name;

    public SchemeTrait(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
