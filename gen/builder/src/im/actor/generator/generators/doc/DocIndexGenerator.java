package im.actor.generator.generators.doc;

import im.actor.generator.scheme.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by ex3ndr on 17.11.14.
 */
public class DocIndexGenerator {

    public static void generate(SchemeDefinition definition, String path) throws IOException {
        String indexTemplate = FileUtils.readFileToString(new File(path + "/index_template.html"));
        String body = "";
        String navigation = "";
        for (SchemeSection section : definition.getSections()) {

            navigation += "<li role=\"presentation\"><a href=\"#" + section.getPkg() + "\">" + section.getName() + "</a></li>";

            body += "<a name=\"" + section.getPkg() + "\"></a><h1 class=\"page-header\">" + section.getName() + "</h1>\n";
            body += section.getUniformedDocs();
            body += "<h3>Reference</h3>";
            body += ReferenceGenerator.generateSectionScheme("reference/", section);
        }

        String indexContent = indexTemplate
                .replace("{body_placeholder}", body)
                .replace("{menu_placeholder}", navigation);
        FileUtils.writeStringToFile(new File(path + "/index.html"), indexContent);
    }
}
