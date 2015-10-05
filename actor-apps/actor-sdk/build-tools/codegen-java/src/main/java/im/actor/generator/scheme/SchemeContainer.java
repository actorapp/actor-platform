package im.actor.generator.scheme;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 14.11.14.
 */
public abstract class SchemeContainer {
    private ArrayList<SchemeAttribute> attributes = new ArrayList<SchemeAttribute>();

    private ArrayList<SchemeDoc> docs = new ArrayList<SchemeDoc>();

    public ArrayList<SchemeAttribute> getAttributes() {
        return attributes;
    }

    public ArrayList<SchemeAttribute> getFilteredAttributes() {
        ArrayList<SchemeAttribute> res = new ArrayList<SchemeAttribute>();
        for (int i = 0; i < attributes.size(); i++) {
            SchemeAttribute a = attributes.get(i);
            if (a.getType() instanceof SchemePrimitiveType && ((SchemePrimitiveType) a.getType()).getName().equals("int32") &&
                    i < attributes.size() - 1) {
                if (attributes.get(i + 1).getType() instanceof SchemeTraitType) {
                    continue;
                }
                if (attributes.get(i + 1).getType() instanceof SchemeOptionalType &&
                        ((SchemeOptionalType) attributes.get(i + 1).getType()).getType() instanceof SchemeTraitType) {
                    continue;
                }
            }
            if (a.getType() instanceof SchemeOptionalType) {
                SchemeType t = ((SchemeOptionalType) a.getType()).getType();
                if (t instanceof SchemePrimitiveType && ((SchemePrimitiveType) t).getName().equals("int32") &&
                        i < attributes.size() - 1) {
                    if (attributes.get(i + 1).getType() instanceof SchemeTraitType) {
                        continue;
                    }
                    if (attributes.get(i + 1).getType() instanceof SchemeOptionalType &&
                            ((SchemeOptionalType) attributes.get(i + 1).getType()).getType() instanceof SchemeTraitType) {
                        continue;
                    }
                }
            }
            res.add(a);
        }
        return res;
    }

    public SchemeAttribute getAttribute(String name) {
        for (SchemeAttribute attribute : attributes) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }
        return null;
    }

    public ArrayList<SchemeDoc> getDocs() {
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
