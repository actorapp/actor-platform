package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.SchemeDefinition;
import im.actor.generator.scheme.SchemeStruct;
import im.actor.generator.scheme.SchemeTrait;

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
        String destFolder = path + "/" + StringJoin.join("/", pkg.split("\\."));
        new File(destFolder).mkdirs();

        for (SchemeTrait u : definition.getAllTraits()) {
            String javaName = JavaConfig.getStructName(u.getName());
            FileGenerator generator = new FileGenerator(destFolder + "/" + javaName + ".java");

            generator.appendLn("package " + pkg + ";");
            generator.appendLn(JavaConfig.NOTICE);
            generator.appendLn();

            for (String im : JavaConfig.IMPORTS) {
                generator.appendLn("import " + im + ";");
            }
            generator.appendLn();
            generator.appendLn("public abstract class " + javaName + " extends BserObject {");
            generator.increaseDepth();
            if (u.isContainer()) {
                generator.appendLn("public static " + javaName + " fromBytes(byte[] src) throws IOException {");
                generator.increaseDepth();
                generator.appendLn("BserValues values = new BserValues(BserParser.deserialize(new DataInput(src, 0, src.length)));");
                generator.appendLn("int key = values.getInt(1);");
                generator.appendLn("byte[] content = values.getBytes(2);");
            } else {
                generator.appendLn("public static " + javaName + " fromBytes(int key, byte[] content) throws IOException {");
                generator.increaseDepth();
            }

            generator.appendLn("switch(key) { ");
            generator.increaseDepth();
            for (SchemeStruct r : definition.getTraitedStructs(u.getName())) {
                generator.appendLn("case " + r.getTraitRef().getKey() + ": return Bser.parse(new " + JavaConfig.getStructName(r.getName()) + "(), content);");
            }
            generator.appendLn("default: return new " + JavaConfig.getStructName(u.getName()) + "Unsupported(key, content);");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.decreaseDepth();
            generator.appendLn("}");

            generator.appendLn("public abstract int getHeader();");
            generator.appendLn();

            generator.appendLn("public byte[] buildContainer() throws IOException {");
            generator.increaseDepth();
            generator.appendLn("DataOutput res = new DataOutput();");
            generator.appendLn("BserWriter writer = new BserWriter(res);");
            generator.appendLn("writer.writeInt(1, getHeader());");
            generator.appendLn("writer.writeBytes(2, toByteArray());");
            generator.appendLn("return res.toByteArray();");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();

            generator.decreaseDepth();
            generator.appendLn("}");
            generator.close();


            generator = new FileGenerator(destFolder + "/" + javaName + "Unsupported.java");

            generator.appendLn("package " + pkg + ";");
            generator.appendLn(JavaConfig.NOTICE);
            generator.appendLn();

            for (String im : JavaConfig.IMPORTS) {
                generator.appendLn("import " + im + ";");
            }
            generator.appendLn();
            generator.appendLn("public class " + javaName + "Unsupported extends " + javaName + " {");
            generator.increaseDepth();
            generator.appendLn();
            generator.appendLn("private int key;");
            generator.appendLn("private byte[] content;");
            generator.appendLn();
            generator.appendLn("public " + javaName + "Unsupported(int key, byte[] content) {");
            generator.increaseDepth();
            generator.appendLn("this.key = key;");
            generator.appendLn("this.content = content;");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();

            generator.appendLn("@Override");
            generator.appendLn("public int getHeader() {");
            generator.increaseDepth();
            generator.appendLn("return this.key;");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();
            generator.appendLn("@Override");
            generator.appendLn("public void parse(BserValues values) throws IOException {");
            generator.increaseDepth();
            generator.appendLn("throw new IOException(\"Parsing is unsupported\");");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();
            generator.appendLn("@Override");
            generator.appendLn("public void serialize(BserWriter writer) throws IOException {");
            generator.increaseDepth();

//            if (u.isContainer()) {
//                generator.appendLn("writer.writeInt(1, key);");
//                generator.appendLn("writer.writeBytes(2, content);");
//            } else {
//                // generator.appendLn("writer.writeRaw(content);");
//            }
            generator.appendLn("writer.writeRaw(content);");

            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();


            generator.decreaseDepth();
            generator.appendLn("}");
            generator.close();
        }

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
            if (u.getTraitRef() != null) {
                generator.appendLn("public class " + javaName + " extends " + JavaConfig.getStructName(u.getTraitRef().getTrait()) + " {");
            } else {
                generator.appendLn("public class " + javaName + " extends BserObject {");
            }
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

            if (u.getTraitRef() != null) {
                generator.appendLn("public int getHeader() {");
                generator.increaseDepth();
                generator.appendLn("return " + u.getTraitRef().getKey() + ";");
                generator.decreaseDepth();
                generator.appendLn("}");
                generator.appendLn();
            }

            ContainerGenerator.generateGetters(generator, definition, u);

            ContainerGenerator.generateDeserialization(generator, u, definition, u.isExpandable());
            ContainerGenerator.generateSerialization(generator, u, definition, u.isExpandable());
            ContainerGenerator.generateToString(generator, u, definition);

            generator.decreaseDepth();
            generator.appendLn("}");
            generator.close();
        }
    }
}
