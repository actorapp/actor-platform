package im.actor.generator.scheme;

/**
* Created by ex3ndr on 14.11.14.
*/
public class SchemeAttribute {
    private String name;
    private int id;
    private SchemeType type;

    public SchemeAttribute(String name, int id, SchemeType type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public SchemeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + "@" + id + ": " + type;
    }
}
