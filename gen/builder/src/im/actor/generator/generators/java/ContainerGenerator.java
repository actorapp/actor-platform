package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.*;

import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class ContainerGenerator {
    public static void generateFields(FileGenerator generator, SchemeContainer container) throws IOException {
        for (SchemeAttribute attribute : container.getAttributes()) {
            generator.append("private ");
            generator.append(JavaConfig.convertType(attribute.getType()));
            generator.appendLn(" " + attribute.getName() + ";");
        }
    }

    public static void generateConstructorArgs(FileGenerator generator, SchemeContainer container) throws IOException {
        boolean isFirst = true;
        for (SchemeAttribute attribute : container.getAttributes()) {
            if (isFirst) {
                isFirst = false;
            } else {
                generator.append(", ");
            }
            generator.append(JavaConfig.convertType(attribute.getType()));
            generator.append(" " + attribute.getName());
        }
    }

    public static void generateConstructorArgsValues(FileGenerator generator, SchemeContainer container) throws IOException {
        boolean isFirst = true;
        for (SchemeAttribute attribute : container.getAttributes()) {
            if (isFirst) {
                isFirst = false;
            } else {
                generator.append(", ");
            }
            generator.append(attribute.getName());
        }
    }

    public static void generateConstructor(FileGenerator generator, SchemeContainer container, String javaName) throws IOException {

        generator.append("public " + javaName + "(");
        ContainerGenerator.generateConstructorArgs(generator, container);
        generator.appendLn(") {");
        generator.increaseDepth();
        for (SchemeAttribute attribute : container.getAttributes()) {
            generator.appendLn("this." + attribute.getName() + " = " + attribute.getName() + ";");
        }
        generator.decreaseDepth();
        // generator.appendLn();
        generator.appendLn("}");
        generator.appendLn();
    }

    public static void generateGetters(FileGenerator generator, SchemeContainer container) throws IOException {
        for (SchemeAttribute attribute : container.getAttributes()) {
            String type = JavaConfig.convertType(attribute.getType());
            String getter = type.equals("boolean") || type.equals("Boolean") ? JavaConfig.getBoolGetterName(attribute.getName()) :
                    JavaConfig.getGetterName(attribute.getName());

            generator.appendLn("public " + type + " " + getter + "() {");
            generator.increaseDepth();
            generator.appendLn("return this." + attribute.getName() + ";");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();
        }
    }

    public static void generateSerialization(FileGenerator generator, SchemeContainer container) throws IOException {
        generator.appendLn("@Override");
        generator.appendLn("public void parse(BserValues values) throws IOException {");
        generator.increaseDepth();
        for (SchemeAttribute attribute : container.getAttributes()) {
            if (attribute.getType() instanceof SchemePrimitiveType) {
                SchemePrimitiveType primitiveType = (SchemePrimitiveType) attribute.getType();
                if (primitiveType.getName().equals("int32")) {
                    generator.appendLn("this." + attribute.getName() + " = values.getInt(" + attribute.getId() + ");");
                } else if (primitiveType.getName().equals("int64")) {
                    generator.appendLn("this." + attribute.getName() + " = values.getLong(" + attribute.getId() + ");");
                } else if (primitiveType.getName().equals("bool")) {
                    generator.appendLn("this." + attribute.getName() + " = values.getBool(" + attribute.getId() + ");");
                } else if (primitiveType.getName().equals("bytes")) {
                    generator.appendLn("this." + attribute.getName() + " = values.getBytes(" + attribute.getId() + ");");
                } else if (primitiveType.getName().equals("string")) {
                    generator.appendLn("this." + attribute.getName() + " = values.getString(" + attribute.getId() + ");");
                } else if (primitiveType.getName().equals("double")) {
                    generator.appendLn("this." + attribute.getName() + " = values.getDouble(" + attribute.getId() + ");");
                } else {
                    throw new IOException();
                }
            } else if (attribute.getType() instanceof SchemeStructType) {
                generator.appendLn("this." + attribute.getName() + " = values.getObj(" + attribute.getId() + ", " +
                        JavaConfig.getStructName(((SchemeStructType) attribute.getType()).getType()) + ".class);");
            } else if (attribute.getType() instanceof SchemeEnumType) {
                SchemeEnumType e = (SchemeEnumType) attribute.getType();
                generator.appendLn("this." + attribute.getName() + " = " + JavaConfig.getEnumName(e.getName()) + ".parse(values.getInt(" + attribute.getId() + "));");
            } else if (attribute.getType() instanceof SchemeOptionalType) {
                SchemeOptionalType optType = (SchemeOptionalType) attribute.getType();
                if (optType.getType() instanceof SchemePrimitiveType) {
                    SchemePrimitiveType primitiveType = (SchemePrimitiveType) optType.getType();
                    if (primitiveType.getName().equals("int32")) {
                        generator.appendLn("this." + attribute.getName() + " = values.optInt(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("int64")) {
                        generator.appendLn("this." + attribute.getName() + " = values.optLong(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("bool")) {
                        generator.appendLn("this." + attribute.getName() + " = values.optBool(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("bytes")) {
                        generator.appendLn("this." + attribute.getName() + " = values.optBytes(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("string")) {
                        generator.appendLn("this." + attribute.getName() + " = values.optString(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("double")) {
                        generator.appendLn("this." + attribute.getName() + " = values.optDouble(" + attribute.getId() + ");");
                    } else {
                        throw new IOException();
                    }
                } else if (optType.getType() instanceof SchemeStructType) {
                    generator.appendLn("this." + attribute.getName() + " = values.optObj(" + attribute.getId() + ", " +
                            JavaConfig.getStructName(((SchemeStructType) optType.getType()).getType()) + ".class);");
                } else if (optType.getType() instanceof SchemeEnumType) {
                    generator.appendLn("int val_" + attribute.getName() + " = values.getInt(" + attribute.getId() + ", 0);");
                    generator.appendLn("if (val_" + attribute.getName() + " != 0) {");
                    generator.increaseDepth();
                    generator.appendLn("this." + attribute.getName() + " = " + JavaConfig.getEnumName(((SchemeEnumType) optType.getType()).getName()) +
                            ".parse(val_" + attribute.getName() + ");");
                    generator.decreaseDepth();
                    generator.appendLn("}");
                } else {
                    throw new IOException();
                }
            } else if (attribute.getType() instanceof SchemeListType) {
                SchemeListType listType = (SchemeListType) attribute.getType();
                if (listType.getType() instanceof SchemePrimitiveType) {
                    SchemePrimitiveType primitiveType = (SchemePrimitiveType) listType.getType();
                    if (primitiveType.getName().equals("int32")) {
                        generator.appendLn("this." + attribute.getName() + " = values.getRepeatedInt(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("int64")) {
                        generator.appendLn("this." + attribute.getName() + " = values.getRepeatedLong(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("bool")) {
                        generator.appendLn("this." + attribute.getName() + " = values.getRepeatedBool(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("bytes")) {
                        generator.appendLn("this." + attribute.getName() + " = values.getRepeatedBytes(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("string")) {
                        generator.appendLn("this." + attribute.getName() + " = values.getRepeatedString(" + attribute.getId() + ");");
                    } else if (primitiveType.getName().equals("double")) {
                        generator.appendLn("this." + attribute.getName() + " = values.getRepeatedDouble(" + attribute.getId() + ");");
                    } else {
                        throw new IOException();
                    }
                } else if (listType.getType() instanceof SchemeStructType) {
                    generator.appendLn("this." + attribute.getName() + " = values.getRepeatedObj(" + attribute.getId() + ", " +
                            JavaConfig.getStructName(((SchemeStructType) listType.getType()).getType()) + ".class);");
                } else {
                    throw new IOException();
                }
            } else {
                throw new IOException();
            }
        }
        generator.decreaseDepth();
        generator.appendLn("}");
        generator.appendLn();
    }

    public static void generateDeserialization(FileGenerator generator, SchemeContainer container) throws IOException {
        generator.appendLn("@Override");
        generator.appendLn("public void serialize(BserWriter writer) throws IOException {");
        generator.increaseDepth();
        for (SchemeAttribute attribute : container.getAttributes()) {
            if (attribute.getType() instanceof SchemePrimitiveType) {
                SchemePrimitiveType primitiveType = (SchemePrimitiveType) attribute.getType();
                if (primitiveType.getName().equals("int32")) {
                    generator.appendLn("writer.writeInt(" + attribute.getId() + ", this." + attribute.getName() + ");");
                } else if (primitiveType.getName().equals("int64")) {
                    generator.appendLn("writer.writeLong(" + attribute.getId() + ", this." + attribute.getName() + ");");
                } else if (primitiveType.getName().equals("bool")) {
                    generator.appendLn("writer.writeBool(" + attribute.getId() + ", this." + attribute.getName() + ");");
                } else if (primitiveType.getName().equals("bytes")) {
                    generator.appendLn("if (this." + attribute.getName() + " == null) {");
                    generator.increaseDepth();
                    generator.appendLn("throw new IOException();");
                    generator.decreaseDepth();
                    generator.appendLn("}");

                    generator.appendLn("writer.writeBytes(" + attribute.getId() + ", this." + attribute.getName() + ");");
                } else if (primitiveType.getName().equals("string")) {
                    generator.appendLn("if (this." + attribute.getName() + " == null) {");
                    generator.increaseDepth();
                    generator.appendLn("throw new IOException();");
                    generator.decreaseDepth();
                    generator.appendLn("}");

                    generator.appendLn("writer.writeString(" + attribute.getId() + ", this." + attribute.getName() + ");");
                } else if (primitiveType.getName().equals("double")) {
                    generator.appendLn("writer.writeDouble(" + attribute.getId() + ", this." + attribute.getName() + ");");
                } else {
                    throw new IOException();
                }
            } else if (attribute.getType() instanceof SchemeStructType) {

                generator.appendLn("if (this." + attribute.getName() + " == null) {");
                generator.increaseDepth();
                generator.appendLn("throw new IOException();");
                generator.decreaseDepth();
                generator.appendLn("}");

                generator.appendLn("writer.writeObject(" + attribute.getId() + ", this." + attribute.getName() + ");");
            } else if (attribute.getType() instanceof SchemeEnumType) {
                SchemeEnumType e = (SchemeEnumType) attribute.getType();

                generator.appendLn("if (this." + attribute.getName() + " == null) {");
                generator.increaseDepth();
                generator.appendLn("throw new IOException();");
                generator.decreaseDepth();
                generator.appendLn("}");

                generator.appendLn("writer.writeInt(" + attribute.getId() + ", this." + attribute.getName() + ".getValue());");
            } else if (attribute.getType() instanceof SchemeOptionalType) {
                SchemeOptionalType optType = (SchemeOptionalType) attribute.getType();
                if (optType.getType() instanceof SchemePrimitiveType) {
                    SchemePrimitiveType primitiveType = (SchemePrimitiveType) optType.getType();
                    if (primitiveType.getName().equals("int32")) {
                        generator.appendLn("if (this." + attribute.getName() + " != null) {");
                        generator.increaseDepth();
                        generator.appendLn("writer.writeInt(" + attribute.getId() + ", this." + attribute.getName() + ");");
                        generator.decreaseDepth();
                        generator.appendLn("}");
                    } else if (primitiveType.getName().equals("int64")) {
                        generator.appendLn("if (this." + attribute.getName() + " != null) {");
                        generator.increaseDepth();
                        generator.appendLn("writer.writeLong(" + attribute.getId() + ", this." + attribute.getName() + ");");
                        generator.decreaseDepth();
                        generator.appendLn("}");
                    } else if (primitiveType.getName().equals("bool")) {
                        generator.appendLn("if (this." + attribute.getName() + " != null) {");
                        generator.increaseDepth();
                        generator.appendLn("writer.writeBool(" + attribute.getId() + ", this." + attribute.getName() + ");");
                        generator.decreaseDepth();
                        generator.appendLn("}");
                    } else if (primitiveType.getName().equals("bytes")) {
                        generator.appendLn("if (this." + attribute.getName() + " != null) {");
                        generator.increaseDepth();
                        generator.appendLn("writer.writeBytes(" + attribute.getId() + ", this." + attribute.getName() + ");");
                        generator.decreaseDepth();
                        generator.appendLn("}");
                    } else if (primitiveType.getName().equals("string")) {
                        generator.appendLn("if (this." + attribute.getName() + " != null) {");
                        generator.increaseDepth();
                        generator.appendLn("writer.writeString(" + attribute.getId() + ", this." + attribute.getName() + ");");
                        generator.decreaseDepth();
                        generator.appendLn("}");
                    } else if (primitiveType.getName().equals("double")) {
                        generator.appendLn("if (this." + attribute.getName() + " != null) {");
                        generator.increaseDepth();
                        generator.appendLn("writer.writeDouble(" + attribute.getId() + ", this." + attribute.getName() + ");");
                        generator.decreaseDepth();
                        generator.appendLn("}");
                    } else {
                        throw new IOException();
                    }
                } else if (optType.getType() instanceof SchemeStructType) {
                    generator.appendLn("if (this." + attribute.getName() + " != null) {");
                    generator.increaseDepth();
                    generator.appendLn("writer.writeObject(" + attribute.getId() + ", this." + attribute.getName() + ");");
                    generator.decreaseDepth();
                    generator.appendLn("}");
                } else if (optType.getType() instanceof SchemeEnumType) {
                    generator.appendLn("if (this." + attribute.getName() + " != null) {");
                    generator.increaseDepth();
                    generator.appendLn("writer.writeInt(" + attribute.getId() + ", this." + attribute.getName() + ".getValue());");
                    generator.decreaseDepth();
                    generator.appendLn("}");
                } else {
                    throw new IOException();
                }
            } else if (attribute.getType() instanceof SchemeListType) {
                SchemeListType listType = (SchemeListType) attribute.getType();
                if (listType.getType() instanceof SchemePrimitiveType) {
                    SchemePrimitiveType primitiveType = (SchemePrimitiveType) listType.getType();
                    if (primitiveType.getName().equals("int32")) {
                        generator.appendLn("writer.writeRepeatedInt(" + attribute.getId() + ", this." + attribute.getName() + ");");
                    } else if (primitiveType.getName().equals("int64")) {
                        generator.appendLn("writer.writeRepeatedLong(" + attribute.getId() + ", this." + attribute.getName() + ");");
                    } else if (primitiveType.getName().equals("bool")) {
                        generator.appendLn("writer.writeRepeatedBool(" + attribute.getId() + ", this." + attribute.getName() + ");");
                    } else if (primitiveType.getName().equals("bytes")) {
                        generator.appendLn("writer.writeRepeatedBytes(" + attribute.getId() + ", this." + attribute.getName() + ");");
                    } else if (primitiveType.getName().equals("string")) {
                        generator.appendLn("writer.writeRepeatedString(" + attribute.getId() + ", this." + attribute.getName() + ");");
                    } else if (primitiveType.getName().equals("double")) {
                        generator.appendLn("writer.writeRepeatedDouble(" + attribute.getId() + ", this." + attribute.getName() + ");");
                    } else {
                        throw new IOException();
                    }
                } else if (listType.getType() instanceof SchemeStructType) {
                    generator.appendLn("writer.writeRepeatedObj(" + attribute.getId() + ", this." + attribute.getName() + ");");
                } else {
                    throw new IOException();
                }
            } else {
                throw new IOException();
            }
        }
        generator.decreaseDepth();
        generator.appendLn("}");
        generator.appendLn();
    }
}
