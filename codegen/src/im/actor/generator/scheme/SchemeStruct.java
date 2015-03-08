package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeStruct extends SchemeContainer implements SchemeRecord {

    private String name;
    private SchemeTraitRef traitRef;

    public SchemeStruct(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public SchemeTraitRef getTraitRef() {
        return traitRef;
    }

    public void setTraitRef(SchemeTraitRef traitRef) {
        this.traitRef = traitRef;
    }

    @Override
    public String toString() {
        return "struct " + name;
    }

}
