package im.actor.generator.generators.doc;

import im.actor.generator.scheme.*;

import java.io.IOException;
import java.util.List;

import static im.actor.generator.generators.doc.HtmlConfig.*;
import static im.actor.generator.generators.doc.HtmlConfig.HEADER;
import static im.actor.generator.generators.doc.HtmlConfig.generateAttributes;

/**
 * Created by ex3ndr on 18.11.14.
 */
public class ReferenceGenerator {

    public static String generateUpdateDefinition(SchemeUpdate u, String pathToReference, boolean useNoBreak) throws IOException {
        return color("update", KEYWORD) + " " + link(u.getName(), pathToReference + "Update" + u.getName() + ".html") + color("@" + hex(u.getHeader()), HEADER) + " " +
                generateAttributes(u.getAttributes(), pathToReference, useNoBreak);
    }

    public static String generateStructDefinition(SchemeStruct u, String pathToReference, boolean useNoBreak) throws IOException {
        return color("struct", KEYWORD) + " " + link(u.getName(), pathToReference + "Struct" + u.getName() + ".html") + " " +
                generateAttributes(u.getAttributes(), pathToReference, useNoBreak);
    }

    public static String generateResponseDefinition(SchemeResponse u, String pathToReference, boolean useNoBreak) throws IOException {
        return color("response", KEYWORD) + " " + link(u.getName(), pathToReference + "Response" + u.getName() + ".html") + " " +
                generateAttributes(u.getAttributes(), pathToReference, useNoBreak);
    }

    public static String generateUpdateBoxDefinition(SchemeUpdateBox u, String pathToReference, boolean useNoBreak) throws IOException {
        return color("update box", KEYWORD) + " " + link(u.getName(), pathToReference + "UpdateBox" + u.getName() + ".html") + " " +
                generateAttributes(u.getAttributes(), pathToReference, useNoBreak);
    }

    public static String generateEnumDefinition(SchemeEnum e, String pathToReference) throws IOException {
        String res = color("enum", KEYWORD) + " " + link(e.getName(), pathToReference + "Enum" + e.getName() + ".html") + " (";
        boolean isFirst = true;
        for (SchemeEnum.Record r : e.getRecord()) {
            if (isFirst) {
                isFirst = false;
            } else {
                res += ", ";
            }
            res += r.getName() + color("@" + r.getId(), HEADER);
        }
        res += ")";
        return res;
    }

    public static String generateRequestDefinition(SchemeRpc u, String pathToReference, boolean useNoBreak) throws IOException {
        String res = color("rpc", KEYWORD) + " " + link(u.getName(), pathToReference + "Rpc" + u.getName() + ".html") + " " +
                generateAttributes(u.getAttributes(), pathToReference, useNoBreak);
        res += " -> ";
        if (u.getResponse() instanceof SchemeRpc.AnonymousResponse) {
            SchemeRpc.AnonymousResponse resp = (SchemeRpc.AnonymousResponse) u.getResponse();
            res += color("tuple", KEYWORD) + color("@" + hex(u.getHeader()), HEADER);
            res += generateAttributes(resp.getResponse().getAttributes(), pathToReference, false);
        } else {
            SchemeRpc.RefResponse refResponse = (SchemeRpc.RefResponse) u.getResponse();
            res += link(refResponse.getName(), "Response" + refResponse.getName() + ".html");
        }
        return res;
    }

    public static String generateParameters(SchemeContainer u, List<SchemeDoc> docs, String pathToReference) throws IOException {
        String body = "";
        body += "<h3>Parameters</h3>";

        if (u.getAttributes().size() > 0) {
            body += "<table class=\"table table-hover\">";
            body += "<tr><th style=\"width:100px;\">Argument</th><th style=\"width:100px;\">Type</th><th>Description</th></tr>";
            for (SchemeDoc doc : docs) {
                if (doc instanceof SchemeDocParameter) {
                    String name = ((SchemeDocParameter) doc).getArgument();
                    String descr = ((SchemeDocParameter) doc).getDescription();
                    body += "<tr>";
                    body += "<td>" + HtmlConfig.bold(name) + "</td>";
                    body += "<td>" + HtmlConfig.typeText(u.getAttribute(name).getType(), pathToReference) + "</td>";
                    body += "<td>" + descr + "</td>";
                }
            }
            body += "</table>";
        } else {
            body += "<h5>No parameters</h5>";
        }

        return body;
    }

    public static String buildDescription(List<SchemeDoc> docs) {
        String res = "";
        for (SchemeDoc doc : docs) {
            if (doc instanceof SchemeDocComment) {
                if (res.length() == 0) {
                    res += " ";
                }
                res += ((SchemeDocComment) doc).getText();
            }
        }
        return res;
    }

    public static String generateShortRecordDefinition(SchemeRecord record, String path) throws IOException {
        if (record instanceof SchemeUpdate) {
            SchemeUpdate u = (SchemeUpdate) record;
            return color("update", KEYWORD) + " " + link(u.getName(), path + "Update" + u.getName() + ".html");
        } else if (record instanceof SchemeStruct) {
            SchemeStruct s = (SchemeStruct) record;
            return color("struct", KEYWORD) + " " + link(s.getName(), path + "Struct" + s.getName() + ".html");
        } else if (record instanceof SchemeEnum) {
            SchemeEnum e = (SchemeEnum) record;
            return color("enum", KEYWORD) + " " + link(e.getName(), path + "Enum" + e.getName() + ".html");
        } else if (record instanceof SchemeRpc) {
            SchemeRpc rpc = (SchemeRpc) record;
            return color("rpc", KEYWORD) + " " + link(rpc.getName(), path + "Rpc" + rpc.getName() + ".html");
        } else if (record instanceof SchemeResponse) {
            SchemeResponse r = (SchemeResponse) record;
            return color("response", KEYWORD) + " " + link(r.getName(), path + "Response" + r.getName() + ".html");
        } else if (record instanceof SchemeUpdateBox) {
            SchemeUpdateBox r = (SchemeUpdateBox) record;
            return color("update box", KEYWORD) + " " + link(r.getName(), path + "UpdateBox" + r.getName() + ".html");
        }
        return "";
    }

    public static String generateRecordDefinition(SchemeRecord record, String path) throws IOException {
        if (record instanceof SchemeUpdate) {
            SchemeUpdate u = (SchemeUpdate) record;
            return ReferenceGenerator.generateUpdateDefinition(u, path, false);
        } else if (record instanceof SchemeStruct) {
            SchemeStruct s = (SchemeStruct) record;
            return ReferenceGenerator.generateStructDefinition(s, path, false);
        } else if (record instanceof SchemeEnum) {
            SchemeEnum e = (SchemeEnum) record;
            return ReferenceGenerator.generateEnumDefinition(e, path);
        } else if (record instanceof SchemeRpc) {
            SchemeRpc rpc = (SchemeRpc) record;
            return ReferenceGenerator.generateRequestDefinition(rpc, path, false);
        } else if (record instanceof SchemeResponse) {
            SchemeResponse r = (SchemeResponse) record;
            return ReferenceGenerator.generateResponseDefinition(r, path, false);
        } else if (record instanceof SchemeUpdateBox) {
            SchemeUpdateBox r = (SchemeUpdateBox) record;
            return ReferenceGenerator.generateUpdateBoxDefinition(r, path, false);
        } else if (record instanceof SchemeWhitespace) {
            return "<br/>";
        } else if (record instanceof SchemeComment) {
            return color("// " + ((SchemeComment) record).getText(), COMMENT);
        }
        return "";
    }

    public static String generateSectionScheme(String path, SchemeSection section) throws IOException {
        String body = "<pre style=\"border:0px; white-space: nowrap;\">\n";
        for (SchemeRecord record : section.getRecords()) {
            body += generateRecordDefinition(record, path) + "<br/>";
        }
        return body + "</pre>";
    }
}
