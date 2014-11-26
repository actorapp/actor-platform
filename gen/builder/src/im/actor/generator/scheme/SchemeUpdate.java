package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeUpdate extends SchemeContainer implements SchemeRecord {
    private String name;
    private int header;

    private List<SchemeDoc> docs = new ArrayList<SchemeDoc>();

    public SchemeUpdate(String name, int header) {
        this.name = name;
        this.header = header;
    }

    public List<SchemeDoc> getDocs() {
        return docs;
    }

    public String getName() {
        return name;
    }

    public int getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return "update " + name;
    }
}
