package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeUpdate extends SchemeContainer implements SchemeRecord {
    private String name;
    private int header;

    public SchemeUpdate(String name, int header) {
        this.name = name;
        this.header = header;
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
