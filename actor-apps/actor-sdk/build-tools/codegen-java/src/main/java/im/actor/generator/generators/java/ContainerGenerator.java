package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.*;

import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class ContainerGenerator {


    public static void generateFields(FileGenerator generator, SchemeDefinition definition, SchemeContainer container) throws IOException {
        for (SchemeAttribute attribute : container.getFilteredAttributes()) {
            generator.append("private ");
            generator.append(JavaConfig.convertType(definition, attribute.getType()));
            generator.appendLn(" " + attribute.getName() + ";");
        }
    }

    public static void generateConstructorArgs(FileGenerator generator, SchemeDefinition definition, SchemeContainer container) throws IOException {
        boolean isFirst = true;
        for (SchemeAttribute attribute : container.getFilteredAttributes()) {
            if (isFirst) {
                isFirst = false;
            } else {
                generator.append(", ");
            }

            SchemeType schemeType = JavaConfig.reduceAlias(attribute.getType(), definition);
            if (schemeType instanceof SchemeOptionalType) {
                generator.append("@Nullable ");
            } else {
                if (!(schemeType instanceof SchemePrimitiveType)) {
                    generator.append("@NotNull ");
                } else {
                    String name = ((SchemePrimitiveType) schemeType).getName();
                    if (name.equals("string") || name.equals("bytes")) {
                        generator.append("@NotNull ");
                    }
                }
            }

            generator.append(JavaConfig.convertType(definition, attribute.getType()));
            generator.append(" " + attribute.getName());
        }
    }

    public static void generateConstructorArgsValues(FileGenerator generator, SchemeContainer container) throws IOException {
        boolean isFirst = true;
        for (SchemeAttribute attribute : container.getFilteredAttributes()) {
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
        for (SchemeAttribute attribute : container.getFilteredAttributes()) {
            generator.appendLn("this." + attribute.getName() + " = " + attribute.getName() + ";");
        }
        generator.decreaseDepth();
        // generator.appendLn();
        generator.appendLn("}");
        generator.appendLn();
    }

    public static void generateGetters(FileGenerator generator, SchemeDefinition definition, SchemeContainer container) throws IOException {
        for (SchemeAttribute attribute : container.getFilteredAttributes()) {
            String type = JavaConfig.convertType(definition, attribute.getType());
            String getter = type.equals("boolean") || type.equals("Boolean") ? JavaConfig.getBoolGetterName(attribute.getName()) :
                    JavaConfig.getGetterName(attribute.getName());

            SchemeType schemeType = JavaConfig.reduceAlias(attribute.getType(), definition);
            if (schemeType instanceof SchemeOptionalType) {
                generator.appendLn("@Nullable");
            } else {
                if (!(schemeType instanceof SchemePrimitiveType)) {
                    generator.appendLn("@NotNull");
                } else {
                    String name = ((SchemePrimitiveType) schemeType).getName();
                    if (name.equals("string") || name.equals("bytes")) {
                        generator.appendLn("@NotNull");
                    }
                }
            }

            generator.appendLn("public " + type + " " + getter + "() {");
            generator.increaseDepth();
            generator.appendLn("return this." + attribute.getName() + ";");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();
        }
    }

    public static void generateToString(FileGenerator generator, SchemeContainer container,
                                        SchemeDefinition definition) throws IOException {
        generator.appendLn("@Override");
        generator.appendLn("public String toString() {");
        generator.increaseDepth();
        generator.append("String res = \"");
        if (container instanceof SchemeUpdate) {
            generator.append("update " + ((SchemeUpdate) container).getName());
        } else if (container instanceof SchemeResponse) {
            generator.append("response " + ((SchemeResponse) container).getName());
        } else if (container instanceof SchemeRpc) {
            generator.append("rpc " + ((SchemeRpc) container).getName());
        } else if (container instanceof SchemeResponseAnonymous) {
            generator.append("tuple " + ((SchemeResponseAnonymous) container).getRpc().getName());
        } else if (container instanceof SchemeUpdateBox) {
            generator.append("update box " + ((SchemeUpdateBox) container).getName());
        } else if (container instanceof SchemeStruct) {
            generator.append("struct " + ((SchemeStruct) container).getName());
        } else {
            throw new IOException();
        }
        generator.appendLn("{\";");
        boolean isFirst = true;
        for (SchemeAttribute attribute : container.getFilteredAttributes()) {
            ParameterCategory category = container.getParameterCategory(attribute.getName());
            if (category == ParameterCategory.HIDDEN ||
                    category == ParameterCategory.DANGER) {
                continue;
            }
            if (isFirst) {
                isFirst = false;
                generator.append("res += \"" + attribute.getName() + "=\" + ");
            } else {
                generator.append("res += \", " + attribute.getName() + "=\" + ");
            }

            generateToStringAppend(generator, attribute.getName(), attribute.getType(), definition, category);
            generator.appendLn(";");
        }
        generator.appendLn("res += \"}\";");
        generator.appendLn("return res;");
        generator.decreaseDepth();
        generator.appendLn("}");
        generator.appendLn();
    }

    public static void generateToStringAppend(FileGenerator generator, String attributeName, SchemeType type,
                                              SchemeDefinition definition,
                                              ParameterCategory category) throws IOException {
        type = JavaConfig.reduceAlias(type, definition);

        if (type instanceof SchemePrimitiveType) {
            SchemePrimitiveType primitiveType = (SchemePrimitiveType) type;
            if (primitiveType.getName().equals("bytes")) {
                if (category == ParameterCategory.COMPACT) {
                    generator.append("byteArrayToStringCompact(this." + attributeName + ")");
                } else {
                    generator.append("byteArrayToString(this." + attributeName + ")");
                }
            } else {
                generator.append("this." + attributeName);
            }
        } else if (type instanceof SchemeStructType) {
            if (category == ParameterCategory.COMPACT) {
                generator.append("(this." + attributeName + " != null ? \"set\":\"empty\")");
            } else {
                generator.append("this." + attributeName);
            }
        } else if (type instanceof SchemeEnumType) {
            generator.append("this." + attributeName);
        } else if (type instanceof SchemeOptionalType) {
            SchemeOptionalType optType = (SchemeOptionalType) type;
            SchemeType childType = JavaConfig.reduceAlias(optType.getType(), definition);

            if (category == ParameterCategory.COMPACT) {
                if (childType instanceof SchemePrimitiveType) {
                    SchemePrimitiveType primitiveType = (SchemePrimitiveType) childType;
                    if (primitiveType.getName().equals("bytes")) {
                        generator.append("byteArrayToStringCompact(this." + attributeName + ")");
                    } else {
                        generator.append("this." + attributeName);
                    }
                } else if (childType instanceof SchemeStructType) {
                    generator.append("(this." + attributeName + " != null ? \"set\":\"empty\")");
                } else if (childType instanceof SchemeEnumType) {
                    generator.append("this." + attributeName);
                } else if (childType instanceof SchemeTraitType) {
                    generator.append("(this." + attributeName + " != null ? \"set\":\"empty\")");
                }
            } else {
                generator.append("this." + attributeName);
            }
        } else if (type instanceof SchemeListType) {

            // TODO: Implement
            if (category == ParameterCategory.COMPACT) {
                generator.append("this." + attributeName + ".size()");
            } else {
                generator.append("this." + attributeName);
            }

//            SchemeListType listType = (SchemeListType) type;
//            SchemeType childType = JavaConfig.reduceAlias(listType.getType(), definition);
//            if (childType instanceof SchemePrimitiveType) {
//                SchemePrimitiveType primitiveType = (SchemePrimitiveType) childType;
//                if (primitiveType.getName().equals("int32")) {
//                    generator.appendLn("writer.writeRepeatedInt(" + attributeId + ", this." + attributeName + ");");
//                } else if (primitiveType.getName().equals("int64")) {
//                    generator.appendLn("writer.writeRepeatedLong(" + attributeId + ", this." + attributeName + ");");
//                } else if (primitiveType.getName().equals("bool")) {
//                    generator.appendLn("writer.writeRepeatedBool(" + attributeId + ", this." + attributeName + ");");
//                } else if (primitiveType.getName().equals("bytes")) {
//                    generator.appendLn("writer.writeRepeatedBytes(" + attributeId + ", this." + attributeName + ");");
//                } else if (primitiveType.getName().equals("string")) {
//                    generator.appendLn("writer.writeRepeatedString(" + attributeId + ", this." + attributeName + ");");
//                } else if (primitiveType.getName().equals("double")) {
//                    generator.appendLn("writer.writeRepeatedDouble(" + attributeId + ", this." + attributeName + ");");
//                } else {
//                    throw new IOException();
//                }
//            } else if (childType instanceof SchemeStructType) {
//                generator.appendLn("writer.writeRepeatedObj(" + attributeId + ", this." + attributeName + ");");
//            } else {
//                throw new IOException();
//            }
        } else if (type instanceof SchemeTraitType) {
            if (category == ParameterCategory.COMPACT) {
                generator.append("(this." + attributeName + " != null ? \"set\":\"empty\")");
            } else {
                generator.append("this." + attributeName);
            }
        } else {
            throw new IOException();
        }
    }

    public static void generateDeserialization(FileGenerator generator, SchemeContainer container,
                                               SchemeDefinition definition) throws IOException {
        generateDeserialization(generator, container, definition, false);
    }

    public static void generateDeserialization(FileGenerator generator, SchemeContainer container,
                                               SchemeDefinition definition, boolean isExpandable) throws IOException {
        generator.appendLn("@Override");
        generator.appendLn("public void parse(BserValues values) throws IOException {");
        generator.increaseDepth();
        for (SchemeAttribute attribute : container.getFilteredAttributes()) {
            generateDeserialization(generator, attribute.getId(), attribute.getName(), attribute.getType(), definition);
        }
        if (isExpandable) {
            generator.appendLn("if (values.hasRemaining()) {");
            generator.increaseDepth();
            generator.appendLn("setUnmappedObjects(values.buildRemaining());");
            generator.decreaseDepth();
            generator.appendLn("}");
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
                    "new " + JavaConfig.getStructName(((SchemeStructType) type).getType()) + "());");
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
                        "new " + JavaConfig.getStructName(((SchemeStructType) childType).getType()) + "());");
            } else if (childType instanceof SchemeEnumType) {
                generator.appendLn("int val_" + attributeName + " = values.getInt(" + attributeId + ", 0);");
                generator.appendLn("if (val_" + attributeName + " != 0) {");
                generator.increaseDepth();
                generator.appendLn("this." + attributeName + " = " + JavaConfig.getEnumName(((SchemeEnumType) childType).getName()) +
                        ".parse(val_" + attributeName + ");");
                generator.decreaseDepth();
                generator.appendLn("}");
            } else if (childType instanceof SchemeTraitType) {

                generator.appendLn("if (values.optBytes(" + attributeId + ") != null) {");
                generator.increaseDepth();
                String traitName = ((SchemeTraitType) childType).getTraitName();
                SchemeTrait trait = definition.getTrait(traitName);
                if (trait.isContainer()) {
                    generator.appendLn("this." + attributeName + " = " + JavaConfig.getStructName(traitName) + ".fromBytes(values.getBytes(" + attributeId + "));");
                } else {
                    generator.appendLn("this." + attributeName + " = " + JavaConfig.getStructName(traitName) + ".fromBytes(values.getInt(" + (attributeId - 1) + "), values.getBytes(" + attributeId + "));");
                }
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
                String typeName = JavaConfig.getStructName(((SchemeStructType) childType).getType());
                generator.appendLn("List<" + typeName + "> _" + attributeName + " = new ArrayList<" + typeName + ">();");
                generator.appendLn("for (int i = 0; i < values.getRepeatedCount(" + attributeId + "); i ++) {");
                generator.increaseDepth();
                generator.appendLn("_" + attributeName + ".add(new " + typeName + "());");
                generator.decreaseDepth();
                generator.appendLn("}");
                generator.appendLn("this." + attributeName + " = values.getRepeatedObj(" + attributeId + ", " +
                        "_" + attributeName + ");");

            } else if (childType instanceof SchemeTraitType) {
                SchemeTraitType traitType = (SchemeTraitType) childType;
                String typeName = JavaConfig.getStructName(traitType.getTraitName());
                generator.appendLn("this." + attributeName + " = new ArrayList<" + typeName + ">();");
                generator.appendLn("for (byte[] b : values.getRepeatedBytes(" + attributeId + ")) {");
                generator.increaseDepth();
                generator.appendLn(attributeName + ".add(" + typeName + ".fromBytes(b));");
                generator.decreaseDepth();
                generator.appendLn("}");
            } else {
                throw new IOException();
            }
        } else if (type instanceof SchemeTraitType) {
            String traitName = ((SchemeTraitType) type).getTraitName();
            SchemeTrait trait = definition.getTrait(traitName);
            if (trait.isContainer()) {
                generator.appendLn("this." + attributeName + " = " + JavaConfig.getStructName(traitName) + ".fromBytes(values.getBytes(" + attributeId + "));");
            } else {
                generator.appendLn("this." + attributeName + " = " + JavaConfig.getStructName(traitName) + ".fromBytes(values.getInt(" + (attributeId - 1) + "), values.getBytes(" + attributeId + "));");
            }
        } else {
            throw new IOException();
        }
    }

    public static void generateSerialization(FileGenerator generator, SchemeContainer container,
                                             SchemeDefinition definition) throws IOException {
        generateSerialization(generator, container, definition, false);
    }

    public static void generateSerialization(FileGenerator generator, SchemeContainer container,
                                             SchemeDefinition definition, boolean isExpandable) throws IOException {
        generator.appendLn("@Override");
        generator.appendLn("public void serialize(BserWriter writer) throws IOException {");
        generator.increaseDepth();
        for (SchemeAttribute attribute : container.getFilteredAttributes()) {
            generateSerialization(generator, attribute.getId(), attribute.getName(), attribute.getType(), definition);
        }

        if (isExpandable) {
            generator.appendLn("if (this.getUnmappedObjects() != null) {");
            generator.increaseDepth();
            generator.appendLn("SparseArray<Object> unmapped = this.getUnmappedObjects();");
            generator.appendLn("for (int i = 0; i < unmapped.size(); i++) {");
            generator.increaseDepth();
            generator.appendLn("int key = unmapped.keyAt(i);");
            generator.appendLn("writer.writeUnmapped(key, unmapped.get(key));");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.decreaseDepth();
            generator.appendLn("}");
        }

//        if (isExpandable) {
//            generator.appendLn("if (w");
//        }

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

                String traitName = ((SchemeTraitType) childType).getTraitName();
                SchemeTrait trait = definition.getTrait(traitName);
                if (trait.isContainer()) {
                    generator.appendLn("writer.writeBytes(" + attributeId + ", this." + attributeName + ".buildContainer());");
                } else {
                    generator.appendLn("writer.writeInt(" + (attributeId - 1) + ", this." + attributeName + ".getHeader());");
                    generator.appendLn("writer.writeBytes(" + attributeId + ", this." + attributeName + ".toByteArray());");
                }

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
            } else if (childType instanceof SchemeTraitType) {
                String traitTypeName = JavaConfig.getStructName(((SchemeTraitType) childType).getTraitName());
                generator.appendLn("for (" + traitTypeName + " i : this." + attributeName + ") {");
                generator.increaseDepth();
                generator.appendLn("writer.writeBytes(" + attributeId + ", i.buildContainer());");
                generator.decreaseDepth();
                generator.appendLn("}");
            } else {
                throw new IOException();
            }
        } else if (type instanceof SchemeTraitType) {
            String traitName = ((SchemeTraitType) type).getTraitName();
            SchemeTrait trait = definition.getTrait(traitName);
            generator.appendLn("if (this." + attributeName + " == null) {");
            generator.increaseDepth();
            generator.appendLn("throw new IOException();");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();
            if (trait.isContainer()) {
                generator.appendLn("writer.writeBytes(" + attributeId + ", this." + attributeName + ".buildContainer());");
            } else {
                generator.appendLn("writer.writeInt(" + (attributeId - 1) + ", this." + attributeName + ".getHeader());");
                generator.appendLn("writer.writeBytes(" + attributeId + ", this." + attributeName + ".toByteArray());");
            }
        } else {
            throw new IOException();
        }
    }
}
