package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeUpdateBox extends SchemeContainer implements SchemeRecord {
    private String name;
    private int header;

    public SchemeUpdateBox(String name, int header) {
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
        return "update box " + name;
    }
}
