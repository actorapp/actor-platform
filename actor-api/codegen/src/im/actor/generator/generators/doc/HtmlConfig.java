package im.actor.generator.generators.doc;

import im.actor.generator.scheme.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by ex3ndr on 18.11.14.
 */
public class HtmlConfig {

    public static final String KEYWORD = "1b2f9b";
    public static final String HEADER = "909090";
    public static final String COMMENT = "618e24";

    public static String bold(String text) {
        return "<font style=\"font-weight: bold;\">" + text + "</font>";
    }

    public static String color(String text, String color) {
        return "<font color='#" + color + "'>" + text + "</font>";
    }

    public static String link(String text, String ref) {
        return "<a href='" + ref + "'>" + text + "</a>";
    }

    public static String noBreak(String text) {
        // return "<span style=\"white-space: nowrap;\">" + text + "</span>";
        return text;
    }

    public static String hex(int v) {
        String res = Integer.toHexString(v);
        if (res.length() == 1) {
            return "0x0" + res;
        } else {
            return "0x" + res;
        }
    }

    public static String typeText(SchemeType type, String referencePath) throws IOException {
        if (type instanceof SchemeEnumType) {
            String name = ((SchemeEnumType) type).getName();
            return link(name, referencePath + "Enum" + name + ".html");
        } else if (type instanceof SchemeListType) {
            return color(bold("list<") + typeText(((SchemeListType) type).getType(), referencePath) + bold(">"), KEYWORD);
        } else if (type instanceof SchemeOptionalType) {
            return color(bold("opt<") + typeText(((SchemeOptionalType) type).getType(), referencePath) + bold(">"), KEYWORD);
        } else if (type instanceof SchemePrimitiveType) {
            return color(bold(((SchemePrimitiveType) type).getName()), KEYWORD);
        } else if (type instanceof SchemeStructType) {
            String name = ((SchemeStructType) type).getType();
            return link(name, referencePath + "Struct" + name + ".html");
        } else {
            throw new IOException();
        }
    }

    public static String generateAttributes(List<SchemeAttribute> attributeList, String referencePath, boolean useNoBreak) throws IOException {
        String res = "(";
        int index = 0;
        for (SchemeAttribute attribute : attributeList) {
            if (useNoBreak) {
                res += "<span style=\"white-space: nowrap;\">";
            }

            res += typeText(attribute.getType(), referencePath) + color("@" + attribute.getId(), HEADER) + " " + attribute.getName();

            // Is Last element
            if (index != attributeList.size() - 1) {
                res += ",";
            }

            if (useNoBreak) {
                res += "</span>";
            }

            if (index != attributeList.size() - 1) {
                res += " ";
            }

            index++;
        }
        res += ")";
        return res;
    }

    public static String scheme(String text) {
        return "<pre style=\"border:0px; white-space: nowrap;\">" + text + "</pre>";
    }

    public static String schemeWrap(String text) {
        return "<pre style=\"border:0px;\">" + text + "</pre>";
    }
}
