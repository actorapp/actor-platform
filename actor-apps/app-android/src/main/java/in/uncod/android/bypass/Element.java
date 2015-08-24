package in.uncod.android.bypass;

import java.util.HashMap;
import java.util.Map;

public class Element {

    public enum Type {

        // Block Element Types
        // Supported
        BLOCK_CODE(0x000),
        BLOCK_QUOTE(0x001),
        HEADER(0x003),
        LIST(0x005),
        LIST_ITEM(0x006),
        // Unsupported
        BLOCK_HTML(0x002),
        HRULE(0x004),
        PARAGRAPH(0x007),
        TABLE(0x008),
        TABLE_CELL(0x009),
        TABLE_ROW(0x00A),

        // Span Element Types
        // Supported
        CODE_SPAN(0x10C),
        LINK(0x111),
        EMPHASIS(0x10E),
        DOUBLE_EMPHASIS(0x10D),
        TRIPLE_EMPHASIS(0x113),
        // Unsupported
        IMAGE(0x10F),
        LINEBREAK(0x110),
        AUTOLINK(0x10B),
        RAW_HTML_TAG(0x112),
        TEXT(0x114);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        private static final Type[] TypeValues = Type.values();

        public static Type fromInteger(int x) {
            for (Type type : TypeValues) {
                if (type.value == x) {
                    return type;
                }
            }
            return null;
        }
    }

    private String text;
    private Map<String, String> attributes = new HashMap<String, String>();
    private Element[] children;
    private Type type;
    private Element parent;

    public Element(String text, int type) {
        this.text = text;
        this.type = Type.fromInteger(type);
    }

    public Element getChildren(int index) {
        return children[index];
    }

    public void setParent(Element element) {
        this.parent = element;
    }

    public void setChildren(Element[] children) {
        this.children = children;
    }

    public void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public Element getParent() {
        return parent;
    }

    public String getText() {
        return text;
    }

    public int size() {
        if (children != null) {
            return children.length;
        }
        return 0;
    }

    public Type getType() {
        return type;
    }

    public boolean isBlockElement() {
        return (type.value & 0x100) == 0x000;
    }

    public boolean isSpanElement() {
        return (type.value & 0x100) == 0x100;
    }

    @Override
    public String toString() {
        String attrs = "";
        for (String key : attributes.keySet()) {
            if (attrs.length() > 0) {
                attrs += ", ";
            }
            attrs += key + " = " + attributes.get(key);
        }
        if (attrs.length() > 0) {
            return "[" + type + "](" + attrs + ")";
        }
        return "[" + type + "]";
    }
}
