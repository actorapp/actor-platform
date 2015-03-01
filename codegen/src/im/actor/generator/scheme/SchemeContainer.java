package im.actor.generator.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public abstract class SchemeContainer {
    private List<SchemeAttribute> attributes = new ArrayList<SchemeAttribute>();

    private List<SchemeDoc> docs = new ArrayList<SchemeDoc>();

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

    public List<SchemeDoc> getDocs() {
        return docs;
    }

    public ParameterCategory getParameterCategory(String name) {
        for (SchemeDoc doc : docs) {
            if (doc instanceof SchemeDocParameter) {
                SchemeDocParameter docParameter = (SchemeDocParameter) doc;
                if (docParameter.getArgument().equals(name)) {
                    return docParameter.getCategory();
                }
            }
        }
        return ParameterCategory.HIDDEN;
    }
}
