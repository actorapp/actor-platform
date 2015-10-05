package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class SchemeResponse extends SchemeBaseResponse {
    private String name;

    public SchemeResponse(String name, int header) {
        super(header);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
