package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.*;

import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class ContainerGenerator {
    public static void generateFields(FileGenerator generator, SchemeDefinition definition, SchemeContainer container) throws IOException {
        for (SchemeAttribute attribute : container.getAttributes()) {
            generator.append("private ");
            generator.append(JavaConfig.convertType(definition, attribute.getType()));
            generator.appendLn(" " + attribute.getName() + ";");
        }
    }

    public static void generateConstructorArgs(FileGenerator generator, SchemeDefinition definition, SchemeContainer container) throws IOException {
        boolean isFirst = true;
        for (SchemeAttribute attribute : container.getAttributes()) {
            if (isFirst) {
                isFirst = false;
            } else {
                generator.append(", ");
            }
            generator.append(JavaConfig.convertType(definition, attribute.getType()));
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

    public static void generateConstructor(FileGenerator generator, SchemeDefinition definition, SchemeContainer container, String javaName) throws IOException {

        generator.append("public " + javaName + "(");
        ContainerGenerator.generateConstructorArgs(generator, definition, container);
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

    public static void generateGetters(FileGenerator generator, SchemeDefinition definition, SchemeContainer container) throws IOException {
        for (SchemeAttribute attribute : container.getAttributes()) {
            String type = JavaConfig.convertType(definition, attribute.getType());
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

    public static void generateSerialization(FileGenerator generator, SchemeContainer container,
                                             SchemeDefinition definition) throws IOException {
        generator.appendLn("@Override");
        generator.appendLn("public void parse(BserValues values) throws IOException {");
        generator.increaseDepth();
        for (SchemeAttribute attribute : container.getAttributes()) {
            generateSerialization(generator, attribute.getId(), attribute.getName(), attribute.getType(), definition);
        }
        generator.decreaseDepth();
        generator.appendLn("}");
        generator.appendLn();
    }

    private static void generateSerialization(FileGenerator generator, int attributeId, String attributeName, SchemeType type,
                                              SchemeDefinition definition) throws IOException {
        type = JavaConfig.reduceAlias(type, definition);
        if (type instanceof SchemePrimitiveType) {
            SchemePrimitiveType primitiveType = (SchemePrimitiveType) type;
            if (primitiveType.getName().equals("int32")) {
                generator.appendLn("this." + attributeName + " = values.getInt(" + attributeId + ");");
            } else if (primitiveType.getName().equals("int64")) {
                generator.appendLn("this." + attributeName + " = values.getLong(" + attributeId + ");");
            } else if (primitiveType.getName().equals("bool")) {
                generator.appendLn("this." + attributeName + " = values.getBool(" + attributeId + ");");
            } else if (primitiveType.getName().equals("bytes")) {
                generator.appendLn("this." + attributeName + " = values.getBytes(" + attributeId + ");");
            } else if (primitiveType.getName().equals("string")) {
                generator.appendLn("this." + attributeName + " = values.getString(" + attributeId + ");");
            } else if (primitiveType.getName().equals("double")) {
                generator.appendLn("this." + attributeName + " = values.getDouble(" + attributeId + ");");
            } else {
                throw new IOException();
            }
        } else if (type instanceof SchemeStructType) {
            generator.appendLn("this." + attributeName + " = values.getObj(" + attributeId + ", " +
                    JavaConfig.getStructName(((SchemeStructType) type).getType()) + ".class);");
        } else if (type instanceof SchemeEnumType) {
            SchemeEnumType e = (SchemeEnumType) type;
            generator.appendLn("this." + attributeName + " = " + JavaConfig.getEnumName(e.getName()) + ".parse(values.getInt(" + attributeId + "));");
        } else if (type instanceof SchemeOptionalType) {
            SchemeOptionalType optType = (SchemeOptionalType) type;
            SchemeType childType = JavaConfig.reduceAlias(optType.getType(), definition);
            if (childType instanceof SchemePrimitiveType) {
                SchemePrimitiveType primitiveType = (SchemePrimitiveType) childType;
                if (primitiveType.getName().equals("int32")) {
                    generator.appendLn("this." + attributeName + " = values.optInt(" + attributeId + ");");
                } else if (primitiveType.getName().equals("int64")) {
                    generator.appendLn("this." + attributeName + " = values.optLong(" + attributeId + ");");
                } else if (primitiveType.getName().equals("bool")) {
                    generator.appendLn("this." + attributeName + " = values.optBool(" + attributeId + ");");
                } else if (primitiveType.getName().equals("bytes")) {
                    generator.appendLn("this." + attributeName + " = values.optBytes(" + attributeId + ");");
                } else if (primitiveType.getName().equals("string")) {
                    generator.appendLn("this." + attributeName + " = values.optString(" + attributeId + ");");
                } else if (primitiveType.getName().equals("double")) {
                    generator.appendLn("this." + attributeName + " = values.optDouble(" + attributeId + ");");
                } else {
                    throw new IOException();
                }
            } else if (childType instanceof SchemeStructType) {
                generator.appendLn("this." + attributeName + " = values.optObj(" + attributeId + ", " +
                        JavaConfig.getStructName(((SchemeStructType) childType).getType()) + ".class);");
            } else if (childType instanceof SchemeEnumType) {
                generator.appendLn("int val_" + attributeName + " = values.getInt(" + attributeId + ", 0);");
                generator.appendLn("if (val_" + attributeName + " != 0) {");
                generator.increaseDepth();
                generator.appendLn("this." + attributeName + " = " + JavaConfig.getEnumName(((SchemeEnumType) childType).getName()) +
                        ".parse(val_" + attributeName + ");");
                generator.decreaseDepth();
                generator.appendLn("}");
            } else if (childType instanceof SchemeTraitType) {
                generator.appendLn("this." + attributeName + " = values.optBytes(" + attributeId + ");");
            } else {
                throw new IOException();
            }
        } else if (type instanceof SchemeListType) {
            SchemeListType listType = (SchemeListType) type;
            SchemeType childType = JavaConfig.reduceAlias(listType.getType(), definition);
            if (childType instanceof SchemePrimitiveType) {
                SchemePrimitiveType primitiveType = (SchemePrimitiveType) childType;
                if (primitiveType.getName().equals("int32")) {
                    generator.appendLn("this." + attributeName + " = values.getRepeatedInt(" + attributeId + ");");
                } else if (primitiveType.getName().equals("int64")) {
                    generator.appendLn("this." + attributeName + " = values.getRepeatedLong(" + attributeId + ");");
                } else if (primitiveType.getName().equals("bool")) {
                    generator.appendLn("this." + attributeName + " = values.getRepeatedBool(" + attributeId + ");");
                } else if (primitiveType.getName().equals("bytes")) {
                    generator.appendLn("this." + attributeName + " = values.getRepeatedBytes(" + attributeId + ");");
                } else if (primitiveType.getName().equals("string")) {
                    generator.appendLn("this." + attributeName + " = values.getRepeatedString(" + attributeId + ");");
                } else if (primitiveType.getName().equals("double")) {
                    generator.appendLn("this." + attributeName + " = values.getRepeatedDouble(" + attributeId + ");");
                } else {
                    throw new IOException();
                }
            } else if (childType instanceof SchemeStructType) {
                generator.appendLn("this." + attributeName + " = values.getRepeatedObj(" + attributeId + ", " +
                        JavaConfig.getStructName(((SchemeStructType) childType).getType()) + ".class);");
            } else {
                throw new IOException();
            }
        } else if (type instanceof SchemeTraitType) {
            generator.appendLn("this." + attributeName + " = values.getBytes(" + attributeId + ");");
        } else {
            throw new IOException();
        }
    }

    public static void generateDeserialization(FileGenerator generator, SchemeContainer container,
                                               SchemeDefinition definition) throws IOException {
        generator.appendLn("@Override");
        generator.appendLn("public void serialize(BserWriter writer) throws IOException {");
        generator.increaseDepth();
        for (SchemeAttribute attribute : container.getAttributes()) {
            generateDeserialization(generator, attribute.getId(), attribute.getName(), attribute.getType(), definition);
        }
        generator.decreaseDepth();
        generator.appendLn("}");
        generator.appendLn();
    }

    private static void generateDeserialization(FileGenerator generator, int attributeId, String attributeName, SchemeType type,
                                                SchemeDefinition definition) throws IOException {
        type = JavaConfig.reduceAlias(type, definition);
        if (type instanceof SchemePrimitiveType) {
            SchemePrimitiveType primitiveType = (SchemePrimitiveType) type;
            if (primitiveType.getName().equals("int32")) {
                generator.appendLn("writer.writeInt(" + attributeId + ", this." + attributeName + ");");
            } else if (primitiveType.getName().equals("int64")) {
                generator.appendLn("writer.writeLong(" + attributeId + ", this." + attributeName + ");");
            } else if (primitiveType.getName().equals("bool")) {
                generator.appendLn("writer.writeBool(" + attributeId + ", this." + attributeName + ");");
            } else if (primitiveType.getName().equals("bytes")) {
                generator.appendLn("if (this." + attributeName + " == null) {");
                generator.increaseDepth();
                generator.appendLn("throw new IOException();");
                generator.decreaseDepth();
                generator.appendLn("}");

                generator.appendLn("writer.writeBytes(" + attributeId + ", this." + attributeName + ");");
            } else if (primitiveType.getName().equals("string")) {
                generator.appendLn("if (this." + attributeName + " == null) {");
                generator.increaseDepth();
                generator.appendLn("throw new IOException();");
                generator.decreaseDepth();
                generator.appendLn("}");

                generator.appendLn("writer.writeString(" + attributeId + ", this." + attributeName + ");");
            } else if (primitiveType.getName().equals("double")) {
                generator.appendLn("writer.writeDouble(" + attributeId + ", this." + attributeName + ");");
            } else {
                throw new IOException();
            }
        } else if (type instanceof SchemeStructType) {

            generator.appendLn("if (this." + attributeName + " == null) {");
            generator.increaseDepth();
            generator.appendLn("throw new IOException();");
            generator.decreaseDepth();
            generator.appendLn("}");

            generator.appendLn("writer.writeObject(" + attributeId + ", this." + attributeName + ");");
        } else if (type instanceof SchemeEnumType) {
            SchemeEnumType e = (SchemeEnumType) type;

            generator.appendLn("if (this." + attributeName + " == null) {");
            generator.increaseDepth();
            generator.appendLn("throw new IOException();");
            generator.decreaseDepth();
            generator.appendLn("}");

            generator.appendLn("writer.writeInt(" + attributeId + ", this." + attributeName + ".getValue());");
        } else if (type instanceof SchemeOptionalType) {
            SchemeOptionalType optType = (SchemeOptionalType) type;
            SchemeType childType = JavaConfig.reduceAlias(optType.getType(), definition);
            if (childType instanceof SchemePrimitiveType) {
                SchemePrimitiveType primitiveType = (SchemePrimitiveType) childType;
                if (primitiveType.getName().equals("int32")) {
                    generator.appendLn("if (this." + attributeName + " != null) {");
                    generator.increaseDepth();
                    generator.appendLn("writer.writeInt(" + attributeId + ", this." + attributeName + ");");
                    generator.decreaseDepth();
                    generator.appendLn("}");
                } else if (primitiveType.getName().equals("int64")) {
                    generator.appendLn("if (this." + attributeName + " != null) {");
                    generator.increaseDepth();
                    generator.appendLn("writer.writeLong(" + attributeId + ", this." + attributeName + ");");
                    generator.decreaseDepth();
                    generator.appendLn("}");
                } else if (primitiveType.getName().equals("bool")) {
                    generator.appendLn("if (this." + attributeName + " != null) {");
                    generator.increaseDepth();
                    generator.appendLn("writer.writeBool(" + attributeId + ", this." + attributeName + ");");
                    generator.decreaseDepth();
                    generator.appendLn("}");
                } else if (primitiveType.getName().equals("bytes")) {
                    generator.appendLn("if (this." + attributeName + " != null) {");
                    generator.increaseDepth();
                    generator.appendLn("writer.writeBytes(" + attributeId + ", this." + attributeName + ");");
                    generator.decreaseDepth();
                    generator.appendLn("}");
                } else if (primitiveType.getName().equals("string")) {
                    generator.appendLn("if (this." + attributeName + " != null) {");
                    generator.increaseDepth();
                    generator.appendLn("writer.writeString(" + attributeId + ", this." + attributeName + ");");
                    generator.decreaseDepth();
                    generator.appendLn("}");
                } else if (primitiveType.getName().equals("double")) {
                    generator.appendLn("if (this." + attributeName + " != null) {");
                    generator.increaseDepth();
                    generator.appendLn("writer.writeDouble(" + attributeId + ", this." + attributeName + ");");
                    generator.decreaseDepth();
                    generator.appendLn("}");
                } else {
                    throw new IOException();
                }
            } else if (childType instanceof SchemeStructType) {
                generator.appendLn("if (this." + attributeName + " != null) {");
                generator.increaseDepth();
                generator.appendLn("writer.writeObject(" + attributeId + ", this." + attributeName + ");");
                generator.decreaseDepth();
                generator.appendLn("}");
            } else if (childType instanceof SchemeEnumType) {
                generator.appendLn("if (this." + attributeName + " != null) {");
                generator.increaseDepth();
                generator.appendLn("writer.writeInt(" + attributeId + ", this." + attributeName + ".getValue());");
                generator.decreaseDepth();
                generator.appendLn("}");
            } else if (childType instanceof SchemeTraitType) {
                generator.appendLn("if (this." + attributeName + " != null) {");
                generator.increaseDepth();
                generator.appendLn("writer.writeBytes(" + attributeId + ", this." + attributeName + ");");
                generator.decreaseDepth();
                generator.appendLn("}");
            } else {
                throw new IOException();
            }
        } else if (type instanceof SchemeListType) {
            SchemeListType listType = (SchemeListType) type;
            SchemeType childType = JavaConfig.reduceAlias(listType.getType(), definition);
            if (childType instanceof SchemePrimitiveType) {
                SchemePrimitiveType primitiveType = (SchemePrimitiveType) childType;
                if (primitiveType.getName().equals("int32")) {
                    generator.appendLn("writer.writeRepeatedInt(" + attributeId + ", this." + attributeName + ");");
                } else if (primitiveType.getName().equals("int64")) {
                    generator.appendLn("writer.writeRepeatedLong(" + attributeId + ", this." + attributeName + ");");
                } else if (primitiveType.getName().equals("bool")) {
                    generator.appendLn("writer.writeRepeatedBool(" + attributeId + ", this." + attributeName + ");");
                } else if (primitiveType.getName().equals("bytes")) {
                    generator.appendLn("writer.writeRepeatedBytes(" + attributeId + ", this." + attributeName + ");");
                } else if (primitiveType.getName().equals("string")) {
                    generator.appendLn("writer.writeRepeatedString(" + attributeId + ", this." + attributeName + ");");
                } else if (primitiveType.getName().equals("double")) {
                    generator.appendLn("writer.writeRepeatedDouble(" + attributeId + ", this." + attributeName + ");");
                } else {
                    throw new IOException();
                }
            } else if (childType instanceof SchemeStructType) {
                generator.appendLn("writer.writeRepeatedObj(" + attributeId + ", this." + attributeName + ");");
            } else {
                throw new IOException();
            }
        } else if (type instanceof SchemeTraitType) {
            generator.appendLn("writer.writeBytes(" + attributeId + ", this." + attributeName + ");");
        } else {
            throw new IOException();
        }
    }
}
