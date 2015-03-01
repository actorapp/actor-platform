package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeStruct extends SchemeContainer implements SchemeRecord {

    private String name;

    public SchemeStruct(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "struct " + name;
    }

}
