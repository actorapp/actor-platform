package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeStruct extends SchemeContainer implements SchemeRecord {

    private String name;
    private List<SchemeDoc> docs;

    public SchemeStruct(String name) {
        this.name = name;
        this.docs = new ArrayList<SchemeDoc>();
    }

    public List<SchemeDoc> getDocs() {
        return docs;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "struct " + name;
    }

}
