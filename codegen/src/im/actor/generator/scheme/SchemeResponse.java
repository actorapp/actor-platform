package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeResponse extends SchemeBaseResponse {
    private String name;

    private List<SchemeDoc> docs = new ArrayList<SchemeDoc>();

    public SchemeResponse(String name, int header) {
        super(header);
        this.name = name;
    }

    public List<SchemeDoc> getDocs() {
        return docs;
    }

    public String getName() {
        return name;
    }
}
