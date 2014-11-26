package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeEnum implements SchemeRecord {

    private String name;
    private List<Record> record = new ArrayList<Record>();

    public SchemeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Record> getRecord() {
        return record;
    }

    @Override
    public String toString() {
        return "enum " + name;
    }

    public static class Record {
        private String name;
        private int id;

        public Record(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }
    }
}
