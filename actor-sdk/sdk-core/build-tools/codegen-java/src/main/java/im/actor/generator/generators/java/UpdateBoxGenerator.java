package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.SchemeDefinition;
import im.actor.generator.scheme.SchemeUpdateBox;

import java.io.File;
import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class UpdateBoxGenerator {
    public static void generate(SchemeDefinition definition, String path) throws IOException {
        String destFolder = path + "/" + JavaConfig.PATH + "/base";
        new File(destFolder).mkdirs();

        for (SchemeUpdateBox u : definition.getAllUpdateBoxes()) {
            String javaName = JavaConfig.getUpdateBoxName(u.getName());
            FileGenerator generator = new FileGenerator(destFolder + "/" + javaName + ".java");
            generator.appendLn("package " + JavaConfig.PACKAGE + ".base;");
            generator.appendLn(JavaConfig.NOTICE);
            generator.appendLn();

            for (String im : JavaConfig.IMPORTS) {
                generator.appendLn("import " + im + ";");
            }
            generator.appendLn("import " + JavaConfig.PACKAGE + ".*;");
            generator.appendLn();
            generator.appendLn("public class " + javaName + " extends RpcScope {");
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
}