package im.actor.generator.generators.doc;

import im.actor.generator.scheme.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static im.actor.generator.generators.doc.HtmlConfig.*;

/**
 * Created by ex3ndr on 18.11.14.
 */
public class DocUpdatesGenerator {
    public static void generate(SchemeDefinition definition, String path) throws IOException {
        String template = FileUtils.readFileToString(new File(path + "/entity_template.html"));
        new File(path + "/reference/").mkdirs();

        for (SchemeSection section : definition.getSections()) {

            String index = "";
            for (SchemeSection section2 : definition.getSections()) {
                String value = "";
                value += (section2 == section) ? "<h3>" + section2.getName() + "</h3>" :
                        "<h5>" + section2.getName() + "</h5>";
                for (SchemeRecord record : section2.getRecords()) {
                    String recVal = ReferenceGenerator.generateShortRecordDefinition(record, "");
                    if (recVal.length() > 0) {
                        value += recVal + "<br/>";
                    }
                }
                value += "<hr>";
                if (section2 == section) {
                    index = value + index;
                } else {
                    index += value;
                }
            }

            for (SchemeUpdate u : section.getAllUpdates()) {
                String fileName = path + "/reference/Update" + u.getName() + ".html";
                String body = "";

                body += createBreadCrumb(section, u.getName());

                body += "<h1>" + u.getName() + " <small>update</small></h1>";
                String description = ReferenceGenerator.buildDescription(u.getDocs());
                body += "<p class=\"lead\">" + description + "</p>";

                body += schemeWrap(ReferenceGenerator.generateUpdateDefinition(u, "", false));

                body += ReferenceGenerator.generateParameters(u, u.getDocs(), "");

                body += "<h3>See also</h3>";
                body += "<a href=\"" + section.getPkg() + ".html\">" + section.getName() + "</a>";

                String content = template
                        .replace("{body_placeholder}", body)
                        .replace("{menu_placeholder}", index);
                FileUtils.writeStringToFile(new File(fileName), content);
            }

            for (SchemeStruct u : section.getAllStructs()) {
                String fileName = path + "/reference/Struct" + u.getName() + ".html";
                String body = "";

                body += createBreadCrumb(section, u.getName());

                body += "<h1>" + u.getName() + " <small>struct</small></h1>";
                String description = ReferenceGenerator.buildDescription(u.getDocs());
                body += "<p class=\"lead\">" + description + "</p>";

                body += schemeWrap(ReferenceGenerator.generateStructDefinition(u, "", false));

                body += ReferenceGenerator.generateParameters(u, u.getDocs(), "");

                body += "<h3>See also</h3>";
                body += "<a href=\"" + section.getPkg() + ".html\">" + section.getName() + "</a>";

                String content = template
                        .replace("{body_placeholder}", body)
                        .replace("{menu_placeholder}", index);
                FileUtils.writeStringToFile(new File(fileName), content);
            }

            for (SchemeBaseResponse u2 : section.getAllResponses()) {
                if (u2 instanceof SchemeResponse) {
                    SchemeResponse u = (SchemeResponse) u2;
                    String fileName = path + "/reference/Response" + u.getName() + ".html";
                    String body = "";

                    body += createBreadCrumb(section, u.getName());

                    body += "<h1>" + u.getName() + " <small>response</small></h1>";
                    String description = ReferenceGenerator.buildDescription(u.getDocs());
                    body += "<p class=\"lead\">" + description + "</p>";

                    body += schemeWrap(ReferenceGenerator.generateResponseDefinition(u, "", false));

                    body += ReferenceGenerator.generateParameters(u, u.getDocs(), "");

                    body += "<h3>See also</h3>";
                    body += "<a href=\"" + section.getPkg() + ".html\">" + section.getName() + "</a>";

                    String content = template
                            .replace("{body_placeholder}", body)
                            .replace("{menu_placeholder}", index);
                    FileUtils.writeStringToFile(new File(fileName), content);
                }
            }

            for (SchemeRpc u : section.getAllRpc()) {
                String fileName = path + "/reference/Rpc" + u.getName() + ".html";
                String body = "";

                body += createBreadCrumb(section, u.getName());

                body += "<h1>" + u.getName() + " <small>rpc</small></h1>";
                String description = ReferenceGenerator.buildDescription(u.getDocs());
                body += "<p class=\"lead\">" + description + "</p>";

                body += schemeWrap(ReferenceGenerator.generateRequestDefinition(u, "", false));

                body += ReferenceGenerator.generateParameters(u, u.getDocs(), "");

                body += "<h3>See also</h3>";
                body += "<a href=\"" + section.getPkg() + ".html\">" + section.getName() + "</a>";

                String content = template
                        .replace("{body_placeholder}", body)
                        .replace("{menu_placeholder}", index);
                FileUtils.writeStringToFile(new File(fileName), content);
            }

            for (SchemeUpdateBox u : section.getAllUpdateBoxes()) {
                String fileName = path + "/reference/UpdateBox" + u.getName() + ".html";
                String body = "";

                body += createBreadCrumb(section, u.getName());

                body += "<h1>" + u.getName() + " <small>update box</small></h1>";
                String description = ReferenceGenerator.buildDescription(u.getDocs());
                body += "<p class=\"lead\">" + description + "</p>";

                body += schemeWrap(ReferenceGenerator.generateUpdateBoxDefinition(u, "", false));

                body += ReferenceGenerator.generateParameters(u, u.getDocs(), "");

                body += "<h3>See also</h3>";
                body += "<a href=\"" + section.getPkg() + ".html\">" + section.getName() + "</a>";

                String content = template
                        .replace("{body_placeholder}", body)
                        .replace("{menu_placeholder}", index);
                FileUtils.writeStringToFile(new File(fileName), content);
            }

            for (SchemeEnum u : section.getAllEnums()) {
                String fileName = path + "/reference/Enum" + u.getName() + ".html";
                String body = "";

                body += createBreadCrumb(section, u.getName());

                body += "<h1>" + u.getName() + " <small>enum</small></h1>";

                body += schemeWrap(ReferenceGenerator.generateEnumDefinition(u, ""));

                body += "<h3>See also</h3>";
                body += "<a href=\"" + section.getPkg() + ".html\">" + section.getName() + "</a>";

                String content = template
                        .replace("{body_placeholder}", body)
                        .replace("{menu_placeholder}", index);
                FileUtils.writeStringToFile(new File(fileName), content);
            }

            String fileName = path + "/reference/" + section.getPkg() + ".html";
            String body = "";
            body += createBreadCrumb(section);
            body += "<h1>" + section.getName() + " <small>" + section.getPkg() + "</small></h1>";
            body += "<h3>Reference</h3>";
            body += ReferenceGenerator.generateSectionScheme("", section);

            String content = template
                    .replace("{body_placeholder}", body)
                    .replace("{menu_placeholder}", index);
            FileUtils.writeStringToFile(new File(fileName), content);
        }

        String index = "";
        for (SchemeSection section2 : definition.getSections()) {
            index += "<h5>" + section2.getName() + "</h5>";
            for (SchemeRecord record : section2.getRecords()) {
                String recVal = ReferenceGenerator.generateShortRecordDefinition(record, "");
                if (recVal.length() > 0) {
                    index += recVal + "<br/>";
                }
            }
            index += "<hr>";
        }

        String fileName = path + "/reference/index.html";
        String body = "";
        body += "<h1>Actor API</h1>";
        body += "<h3>Reference</h3>";

        for (SchemeSection section : definition.getSections()) {
            body += "<h4>" + section.getName() + "</h4>";
            body += ReferenceGenerator.generateSectionScheme("", section);
        }

        String content = template
                .replace("{body_placeholder}", body)
                .replace("{menu_placeholder}", index);
        FileUtils.writeStringToFile(new File(fileName), content);
    }

    private static String createBreadCrumb(SchemeSection section, String entityName) {
        return "<ol class=\"breadcrumb\" style=\"margin-top:14px\">\n" +
                "  <li><a href=\"../index.html\">Actor API</a></li>\n" +
                "  <li><a href=\"" + section.getPkg() + ".html\">" + section.getName() + "</a></li>\n" +
                "  <li class=\"active\">" + entityName + "</li>\n" +
                "</ol>";
    }

    private static String createBreadCrumb(SchemeSection section) {
        return "<ol class=\"breadcrumb\" style=\"margin-top:14px\">\n" +
                "  <li><a href=\"../index.html\">Actor API</a></li>\n" +
                "  <li class=\"active\">" + section.getName() + "</li>\n" +
                "</ol>";
    }
}