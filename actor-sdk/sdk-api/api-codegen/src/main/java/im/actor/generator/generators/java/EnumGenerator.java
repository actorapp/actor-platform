package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.SchemeDefinition;
import im.actor.generator.scheme.SchemeEnum;

import java.io.File;
import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class EnumGenerator {
    public static void generate(SchemeDefinition definition, String path) throws IOException {
        generate(definition, path, JavaConfig.PACKAGE);
    }

    public static void generate(SchemeDefinition definition, String path, String pkg) throws IOException {
        String destFolder = path + "/" + StringJoin.join("/", pkg.split("\\."));
        new File(destFolder).mkdirs();

        for (SchemeEnum e : definition.getAllEnums()) {
            String javaName = JavaConfig.getEnumName(e.getName());
            FileGenerator generator = new FileGenerator(destFolder + "/" + javaName + ".java");
            generator.appendLn("package " + pkg + ";");
            generator.appendLn(JavaConfig.NOTICE);
            generator.appendLn();
            generator.appendLn("import java.io.IOException;");
            generator.appendLn();
            generator.appendLn("public enum " + javaName + " {");
            generator.appendLn();
            generator.increaseDepth();
            boolean isFirst = true;
            for (SchemeEnum.Record r : e.getRecord()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    generator.appendLn(",");
                }
                generator.append(JavaConfig.getEnumRecordName(r) + "(" + r.getId() + ")");
            }
            generator.appendLn(",");
            generator.append("UNSUPPORTED_VALUE(-1)");
            generator.appendLn(";");
            generator.appendLn();

            generator.appendLn("private int value;");
            generator.appendLn();
            generator.appendLn(javaName + "(int value) {");
            generator.increaseDepth();
            generator.appendLn("this.value = value;");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();

            generator.appendLn("public int getValue() {");
            generator.increaseDepth();
            generator.appendLn("return value;");
            generator.decreaseDepth();
            generator.appendLn("}");

            generator.appendLn();
            generator.appendLn("public static " + javaName + " parse(int value) throws IOException {");
            generator.increaseDepth();
            generator.appendLn("switch(value) {");
            generator.increaseDepth();
            for (SchemeEnum.Record r : e.getRecord()) {
                generator.appendLn("case " + r.getId() + ": return " + javaName + "." + JavaConfig.getEnumRecordName(r) + ";");
            }
            generator.appendLn("default: return " + javaName + ".UNSUPPORTED_VALUE;");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.close();
        }
    }
}
