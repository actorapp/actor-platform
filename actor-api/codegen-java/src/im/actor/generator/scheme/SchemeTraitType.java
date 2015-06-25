package im.actor.generator.scheme;

/**
 * Created by ex3ndr on 22.11.14.
 */
public class SchemeTraitType extends SchemeType {
    private String traitName;

    public SchemeTraitType(String traitName) {
        this.traitName = traitName;
    }

    public String getTraitName() {
        return traitName;
    }
}
