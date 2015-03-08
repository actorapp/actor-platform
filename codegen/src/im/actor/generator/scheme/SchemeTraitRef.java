package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class SchemeTraitRef {
    private int key;
    private String trait;

    public SchemeTraitRef(int key, String trait) {
        this.key = key;
        this.trait = trait;
    }

    public int getKey() {
        return key;
    }

    public String getTrait() {
        return trait;
    }
}
