package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.SchemeDefinition;
import im.actor.generator.scheme.SchemeUpdate;
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
            generator.appendLn("return Bser.parse(" + javaName + ".class, data);");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();
            ContainerGenerator.generateFields(generator, u);

            generator.appendLn();

            if (u.getAttributes().size() > 0) {
                ContainerGenerator.generateConstructor(generator, u, javaName);
            }

            generator.appendLn("public " + javaName + "() {");
            generator.appendLn();
            generator.appendLn("}");
            generator.appendLn();

            ContainerGenerator.generateGetters(generator, u);

            ContainerGenerator.generateSerialization(generator, u);
            ContainerGenerator.generateDeserialization(generator, u);

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