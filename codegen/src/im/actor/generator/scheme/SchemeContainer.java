package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public abstract class SchemeContainer {
    private List<SchemeAttribute> attributes = new ArrayList<SchemeAttribute>();

    public List<SchemeAttribute> getAttributes() {
        return attributes;
    }

    public SchemeAttribute getAttribute(String name) {
        for (SchemeAttribute attribute : attributes) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }
        return null;
    }
}
