package im.actor.generator.generators.java;

import im.actor.generator.scheme.*;

import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class JavaConfig {
    public static final String PACKAGE = "im.actor.api.scheme";
    public static final String PATH = String.join("/", PACKAGE.split("\\."));
    public static final String[] IMPORTS = new String[]{
            "com.droidkit.bser.Bser",
            "com.droidkit.bser.BserObject",
            "com.droidkit.bser.BserValues",
            "com.droidkit.bser.BserWriter",
            "java.io.IOException",
            "im.actor.api.parser.*",
            "java.util.List"
    };

    public static String getEnumName(String e) {
        return e;
    }

    public static String getEnumRecordName(SchemeEnum.Record record) {
        // TODO: Implement dividing to words
        return record.getName().toUpperCase();
    }

    public static String getStructName(String e) {
        return e;
    }

    public static String getUpdateName(SchemeUpdate e) {
        return "Update" + e.getName();
    }

    public static String getUpdateBoxName(String e) {
        return e;
    }

    public static String getRequestName(String name) {
        if (name.startsWith("Request")) {
            return name;
        }
        return "Request" + name;
    }

    public static String getResponseName(String name) {
        return "Response" + name;
    }

    public static String getAnonymousResponseName(String methodName) {
        if (methodName.startsWith("Request")) {
            return "Response" + methodName.substring("Request".length());
        }
        return "Response" + methodName;
    }

    public static String getRequestsName(String v) {
        return v.substring(0, 1).toLowerCase() + v.substring(1);
    }

    public static String getGetterName(String v) {
        return "get" + v.substring(0, 1).toUpperCase() + v.substring(1);
    }

    public static String getBoolGetterName(String v) {
        return v;
    }


    public static String mapPrimitiveType(SchemePrimitiveType primitiveType) throws IOException {
        if (primitiveType.getName().equals("int32")) {
            return "int";
        } else if (primitiveType.getName().equals("int64")) {
            return "long";
        } else if (primitiveType.getName().equals("bytes")) {
            return "byte[]";
        } else if (primitiveType.getName().equals("string")) {
            return "String";
        } else if (primitiveType.getName().equals("double")) {
            return "double";
        } else if (primitiveType.getName().equals("bool")) {
            return "boolean";
        } else {
            throw new IOException();
        }
    }

    public static String mapPrimitiveReferenceType(SchemePrimitiveType primitiveType) throws IOException {
        if (primitiveType.getName().equals("int32")) {
            return "Integer";
        } else if (primitiveType.getName().equals("int64")) {
            return "Long";
        } else if (primitiveType.getName().equals("bytes")) {
            return "byte[]";
        } else if (primitiveType.getName().equals("string")) {
            return "String";
        } else if (primitiveType.getName().equals("double")) {
            return "Double";
        } else if (primitiveType.getName().equals("bool")) {
            return "Boolean";
        } else {
            throw new IOException();
        }
    }

    public static String convertType(SchemeDefinition definition, SchemeType type) throws IOException {
        type = reduceAlias(type, definition);
        if (type instanceof SchemePrimitiveType) {
            return JavaConfig.mapPrimitiveType((SchemePrimitiveType) type);
        } else if (type instanceof SchemeStructType) {
            return getStructName(((SchemeStructType) type).getType());
        } else if (type instanceof SchemeListType) {
            SchemeListType listType = (SchemeListType) type;
            SchemeType childType = reduceAlias(listType.getType(), definition);
            if (childType instanceof SchemePrimitiveType) {
                return "List<" + mapPrimitiveReferenceType((SchemePrimitiveType) childType) + ">";
            } else if (childType instanceof SchemeStructType) {
                return "List<" + getStructName(((SchemeStructType) childType).getType()) + ">";
            } else {
                throw new IOException();
            }

        } else if (type instanceof SchemeOptionalType) {
            SchemeOptionalType optionalType = (SchemeOptionalType) type;
            SchemeType childType = reduceAlias(optionalType.getType(), definition);
            if (childType instanceof SchemePrimitiveType) {
                return mapPrimitiveReferenceType((SchemePrimitiveType) childType);
            } else if (childType instanceof SchemeStructType) {
                return getStructName(((SchemeStructType) childType).getType());
            } else if (childType instanceof SchemeEnumType) {
                return getEnumName(((SchemeEnumType) childType).getName());
            } else if (childType instanceof SchemeTraitType) {
                return "byte[]";
            } else {
                throw new IOException();
            }
        } else if (type instanceof SchemeEnumType) {
            return getEnumName(((SchemeEnumType) type).getName());
        } else if (type instanceof SchemeTraitType) {
            return "byte[]";
        } else {
            throw new IOException();
        }
    }

    public static SchemeType reduceAlias(SchemeType type, SchemeDefinition definition) {
        if (type instanceof SchemeAliasType) {
            return reduceAlias(definition.getAliases().get(((SchemeAliasType) type).getName()), definition);
        } else {
            return type;
        }
    }
}
