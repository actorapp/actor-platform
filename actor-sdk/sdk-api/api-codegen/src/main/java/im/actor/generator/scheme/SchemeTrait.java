package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 22.11.14.
 */
public class SchemeTrait implements SchemeRecord {
    private String name;
    private boolean isContainer;

    public SchemeTrait(String name, boolean isContainer) {
        this.name = name;
        this.isContainer = isContainer;
    }

    public String getName() {
        return name;
    }

    public boolean isContainer() {
        return isContainer;
    }
}
