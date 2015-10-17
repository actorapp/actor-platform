package im.actor.generator.generators;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.*;

import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class ProtoBufGenerator {
    public static void generate(SchemeDefinition definition, String fileName) throws IOException {
        FileGenerator generator = new FileGenerator(fileName);
        for (SchemeSection section : definition.getSections()) {
            generator.appendLn("//////////////////////////////////////////////////////");
            generator.appendLn("// " + section.getName());
            generator.appendLn("//////////////////////////////////////////////////////");
            for (String s : section.getDocs()) {
                generator.appendLn("//" + s);
            }
            generator.appendLn();
            for (SchemeRecord record : section.getRecords()) {
                if (record instanceof SchemeEnum) {
                    SchemeEnum sEnum = (SchemeEnum) record;
                    generator.appendLn("enum " + sEnum.getName() + " {");
                    generator.increaseDepth();
                    for (SchemeEnum.Record enrec : sEnum.getRecord()) {
                        generator.appendLn(enrec.getName().toUpperCase() + " = " + enrec.getId() + ";");
                    }
                    generator.decreaseDepth();
                    generator.appendLn("}");
                    generator.appendLn();
                } else if (record instanceof SchemeStruct) {
                    SchemeStruct sEnum = (SchemeStruct) record;
                    for (SchemeDoc d : sEnum.getDocs()) {
                        if (d instanceof SchemeDocComment) {
                            generator.appendLn("// " + ((SchemeDocComment) d).getText());
                        } else if (d instanceof SchemeDocParameter) {
                            generator.appendLn("// " + ((SchemeDocParameter) d).getArgument() + " " + ((SchemeDocParameter) d).getDescription());
                        }
                    }
                    generator.appendLn("message " + sEnum.getName() + " {");
                    generator.increaseDepth();
                    appendContents(definition, sEnum, generator);
                    generator.decreaseDepth();
                    generator.appendLn("}");
                    generator.appendLn();
                } else if (record instanceof SchemeUpdate) {
                    SchemeUpdate update = (SchemeUpdate) record;
                    generator.appendLn("// API#0x" + toHex(update.getHeader()));
                    for (SchemeDoc d : update.getDocs()) {
                        if (d instanceof SchemeDocComment) {
                            generator.appendLn("// " + ((SchemeDocComment) d).getText());
                        } else if (d instanceof SchemeDocParameter) {
                            generator.appendLn("// " + ((SchemeDocParameter) d).getArgument() + " " + ((SchemeDocParameter) d).getDescription());
                        }
                    }
                    generator.appendLn("message Update" + update.getName() + " {");
                    generator.increaseDepth();
                    appendContents(definition, update, generator);
                    generator.decreaseDepth();
                    generator.appendLn("}");
                    generator.appendLn();
                } else if (record instanceof SchemeUpdateBox) {
                    SchemeUpdateBox update = (SchemeUpdateBox) record;
                    generator.appendLn("// API#0x" + toHex(update.getHeader()));
                    for (SchemeDoc d : update.getDocs()) {
                        if (d instanceof SchemeDocComment) {
                            generator.appendLn("// " + ((SchemeDocComment) d).getText());
                        } else if (d instanceof SchemeDocParameter) {
                            generator.appendLn("// " + ((SchemeDocParameter) d).getArgument() + " " + ((SchemeDocParameter) d).getDescription());
                        }
                    }
                    generator.appendLn("message Update" + update.getName() + " {");
                    generator.increaseDepth();
                    appendContents(definition, update, generator);
                    generator.decreaseDepth();
                    generator.appendLn("}");
                    generator.appendLn();
                } else if (record instanceof SchemeResponse) {
                    SchemeResponse update = (SchemeResponse) record;
                    generator.appendLn("// API#0x" + toHex(update.getHeader()));
                    for (SchemeDoc d : update.getDocs()) {
                        if (d instanceof SchemeDocComment) {
                            generator.appendLn("// " + ((SchemeDocComment) d).getText());
                        } else if (d instanceof SchemeDocParameter) {
                            generator.appendLn("// " + ((SchemeDocParameter) d).getArgument() + " " + ((SchemeDocParameter) d).getDescription());
                        }
                    }
                    generator.appendLn("message Response" + update.getName() + " {");
                    generator.increaseDepth();
                    appendContents(definition, update, generator);
                    generator.decreaseDepth();
                    generator.appendLn("}");
                    generator.appendLn();
                } else if (record instanceof SchemeRpc) {
                    SchemeRpc rpc = (SchemeRpc) record;
                    generator.appendLn("// API#0x" + toHex(rpc.getHeader()));
                    if (rpc.getResponse() instanceof SchemeRpc.RefResponse) {
                        SchemeRpc.RefResponse resp = (SchemeRpc.RefResponse) rpc.getResponse();
                        generator.appendLn("// Response" + resp.getName());
                    } else {
                        generator.appendLn("// " + responseName(rpc.getName()));
                    }
                    for (SchemeDoc d : rpc.getDocs()) {
                        if (d instanceof SchemeDocComment) {
                            generator.appendLn("// " + ((SchemeDocComment) d).getText());
                        } else if (d instanceof SchemeDocParameter) {
                            generator.appendLn("// " + ((SchemeDocParameter) d).getArgument() + " " + ((SchemeDocParameter) d).getDescription());
                        }
                    }
                    generator.appendLn("message " + requestName(rpc.getName()) + " {");
                    generator.increaseDepth();
                    appendContents(definition, rpc, generator);
                    generator.decreaseDepth();
                    generator.appendLn("}");
                    generator.appendLn();

                    if (rpc.getResponse() instanceof SchemeRpc.AnonymousResponse) {
                        SchemeRpc.AnonymousResponse resp = (SchemeRpc.AnonymousResponse) rpc.getResponse();
                        generator.appendLn("// API#0x" + toHex(resp.getResponse().getHeader()));
                        generator.appendLn("message " + responseName(rpc.getName()) + " {");
                        generator.increaseDepth();
                        appendContents(definition, resp.getResponse(), generator);
                        generator.decreaseDepth();
                        generator.appendLn("}");
                        generator.appendLn();
                    }
                }
            }
        }
        generator.close();
    }


    private static void appendContents(SchemeDefinition definition, SchemeContainer container, FileGenerator generator) throws IOException {
        for (SchemeAttribute enrec : container.getAttributes()) {
            generator.appendLn(getType(definition, enrec.getType(), true) + " " + enrec.getName() + " = " + enrec.getId() + ";");
        }
    }

    private static String getType(SchemeDefinition definition, SchemeType attribute, boolean isFirst) throws IOException {
        if (attribute instanceof SchemePrimitiveType) {
            return (isFirst ? "required " : "") + ((SchemePrimitiveType) attribute).getName();
        } else if (attribute instanceof SchemeOptionalType) {
            return "optional " + getType(definition, ((SchemeOptionalType) attribute).getType(), false);
        } else if (attribute instanceof SchemeListType) {
            return "repeated " + getType(definition, ((SchemeListType) attribute).getType(), false);
        } else if (attribute instanceof SchemeStructType) {
            return (isFirst ? "required " : "") + ((SchemeStructType) attribute).getType();
        } else if (attribute instanceof SchemeEnumType) {
            return (isFirst ? "required " : "") + ((SchemeEnumType) attribute).getName();
        } else if (attribute instanceof SchemeAliasType) {
            return getType(definition, definition.getAliases().get(((SchemeAliasType) attribute).getName()), isFirst);
        } else if (attribute instanceof SchemeTraitType) {
            return (isFirst ? "required bytes" : "bytes");
        } else {
            throw new IOException();
        }
    }

    private static String toHex(int val) {
        String res = Integer.toHexString(val);
        if (res.length() < 2) {
            res = "0" + res;
        }
        return res.toUpperCase();
    }

    private static String requestName(String name) {
        if (name.startsWith("Request")) {
            return name;
        } else {
            return "Request" + name;
        }
    }

    private static String responseName(String name) {
        if (name.startsWith("Request")) {
            return "Response" + name.substring("Request".length());
        } else {
            return "Response" + name;
        }
    }
}
