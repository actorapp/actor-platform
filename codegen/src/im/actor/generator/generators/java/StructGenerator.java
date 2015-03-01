package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.SchemeDefinition;
import im.actor.generator.scheme.SchemeStruct;

import java.io.File;
import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class StructGenerator {
    public static void generate(SchemeDefinition definition, String path) throws IOException {
        generate(definition, path, JavaConfig.PACKAGE);
    }

    public static void generate(SchemeDefinition definition, String path, String pkg) throws IOException {
        String destFolder = path + "/" + String.join("/", pkg.split("\\."));
        new File(destFolder).mkdirs();

        for (SchemeStruct u : definition.getAllStructs()) {
            String javaName = JavaConfig.getStructName(u.getName());
            FileGenerator generator = new FileGenerator(destFolder + "/" + javaName + ".java");
            generator.appendLn("package " + pkg + ";");
            generator.appendLn(JavaConfig.NOTICE);
            generator.appendLn();

            for (String im : JavaConfig.IMPORTS) {
                generator.appendLn("import " + im + ";");
            }
            generator.appendLn();
            generator.appendLn("public class " + javaName + " extends BserObject {");
            generator.increaseDepth();
            generator.appendLn();
            ContainerGenerator.generateFields(generator, definition, u);

            generator.appendLn();

            if (u.getAttributes().size() > 0) {
                ContainerGenerator.generateConstructor(generator, definition, u, javaName);
            }

            generator.appendLn("public " + javaName + "() {");
            generator.appendLn();
            generator.appendLn("}");
            generator.appendLn();

            ContainerGenerator.generateGetters(generator, definition, u);

            ContainerGenerator.generateSerialization(generator, u, definition);
            ContainerGenerator.generateDeserialization(generator, u, definition);
            ContainerGenerator.generateToString(generator, u, definition);

            generator.decreaseDepth();
            generator.appendLn("}");
            generator.close();
        }
    }
}
