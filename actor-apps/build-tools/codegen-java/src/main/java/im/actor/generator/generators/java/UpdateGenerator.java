package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.SchemeDefinition;
import im.actor.generator.scheme.SchemeUpdate;

import java.io.File;
import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class UpdateGenerator {
    public static void generate(SchemeDefinition definition, String path) throws IOException {
        String destFolder = path + "/" + JavaConfig.PATH + "/updates";
        new File(destFolder).mkdirs();

        for (SchemeUpdate u : definition.getAllUpdates()) {
            String javaName = JavaConfig.getUpdateName(u);
            FileGenerator generator = new FileGenerator(destFolder + "/" + javaName + ".java");
            generator.appendLn("package " + JavaConfig.PACKAGE + ".updates;");
            generator.appendLn(JavaConfig.NOTICE);
            generator.appendLn();

            for (String im : JavaConfig.IMPORTS) {
                generator.appendLn("import " + im + ";");
            }
            generator.appendLn("import " + JavaConfig.PACKAGE + ".*;");
            generator.appendLn();
            generator.appendLn("public class " + javaName + " extends Update {");
            generator.increaseDepth();
            generator.appendLn();
            generator.appendLn("public static final int HEADER = 0x" + Integer.toHexString(u.getHeader()) + ";");
            generator.appendLn("public static " + javaName + " fromBytes(byte[] data) throws IOException {");
            generator.increaseDepth();
            generator.appendLn("return Bser.parse(new " + javaName + "(), data);");
            generator.decreaseDepth();
            generator.appendLn("}");
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

            ContainerGenerator.generateDeserialization(generator, u, definition);
            ContainerGenerator.generateSerialization(generator, u, definition);
            ContainerGenerator.generateToString(generator, u, definition);

            generator.appendLn("@Override");
            generator.appendLn("public int getHeaderKey() {");
            generator.increaseDepth();
            generator.appendLn("return HEADER;");
            generator.decreaseDepth();
            generator.appendLn("}");

            generator.decreaseDepth();
            generator.appendLn("}");
            generator.close();
        }
    }

    public static void generateParser(SchemeDefinition definition, String path) throws IOException {
        String destFolder = path + "/" + JavaConfig.PATH + "/parser/";
        new File(destFolder).mkdirs();
        FileGenerator generator = new FileGenerator(destFolder + "/UpdatesParser.java");
        generator.appendLn("package " + JavaConfig.PACKAGE + ".parser;");
        generator.appendLn(JavaConfig.NOTICE);
        generator.appendLn();

        for (String im : JavaConfig.IMPORTS) {
            generator.appendLn("import " + im + ";");
        }
        generator.appendLn("import " + JavaConfig.PACKAGE + ".updates.*;");
        generator.appendLn();
        generator.appendLn("public class UpdatesParser extends BaseParser<Update> {");
        generator.increaseDepth();
        generator.appendLn("@Override");
        generator.appendLn("public Update read(int type, byte[] payload) throws IOException {");
        generator.increaseDepth();
        generator.appendLn("switch(type) {");
        generator.increaseDepth();
        for (SchemeUpdate u : definition.getAllUpdates()) {
            String javaName = JavaConfig.getUpdateName(u);
            generator.appendLn("case " + u.getHeader() + ": return " + javaName + ".fromBytes(payload);");
        }
        generator.decreaseDepth();
        generator.appendLn("}");
        generator.appendLn("throw new IOException();");
        generator.decreaseDepth();
        generator.appendLn("}");
        generator.decreaseDepth();
        generator.appendLn("}");
        generator.close();
    }
}