package im.actor.generator.generators.java;

import im.actor.generator.FileGenerator;
import im.actor.generator.scheme.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public class RequestGenerator {
    public static void generate(SchemeDefinition definition, String path) throws IOException {
        String destFolder = path + "/" + JavaConfig.PATH + "/rpc/";
        new File(destFolder).mkdirs();

        for (SchemeRpc u : definition.getAllRpc()) {
            String javaName = JavaConfig.getRequestName(u.getName());

            String responseJavaName;
            if (u.getResponse() instanceof SchemeRpc.RefResponse) {
                responseJavaName = JavaConfig.getResponseName(((SchemeRpc.RefResponse) u.getResponse()).getName());
            } else {
                responseJavaName = JavaConfig.getAnonymousResponseName(u.getName());
            }

            FileGenerator generator = new FileGenerator(destFolder + "/" + javaName + ".java");
            generator.appendLn("package " + JavaConfig.PACKAGE + ".rpc;");
            generator.appendLn(JavaConfig.NOTICE);
            generator.appendLn();
            for (String im : JavaConfig.IMPORTS) {
                generator.appendLn("import " + im + ";");
            }
            generator.appendLn("import " + JavaConfig.PACKAGE + ".*;");
            generator.appendLn();
            generator.appendLn("public class " + javaName + " extends Request<" + responseJavaName + "> {");
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

        for (SchemeBaseResponse u : definition.getAllResponses()) {
            String javaName;
            if (u instanceof SchemeResponse) {
                javaName = JavaConfig.getResponseName(((SchemeResponse) u).getName());
            } else {
                SchemeResponseAnonymous anonymous = (SchemeResponseAnonymous) u;
                javaName = JavaConfig.getAnonymousResponseName(anonymous.getRpc().getName());
            }
            FileGenerator generator = new FileGenerator(destFolder + "/" + javaName + ".java");
            generator.appendLn("package " + JavaConfig.PACKAGE + ".rpc;");
            generator.appendLn(JavaConfig.NOTICE);
            generator.appendLn();
            for (String im : JavaConfig.IMPORTS) {
                generator.appendLn("import " + im + ";");
            }
            generator.appendLn("import " + JavaConfig.PACKAGE + ".*;");
            generator.appendLn();
            generator.appendLn("public class " + javaName + " extends Response {");
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

    public static void generateRpcList(SchemeDefinition definition, String path) throws IOException {
        String destFolder = path + "/" + JavaConfig.PATH + "/";
        new File(destFolder).mkdirs();
        FileGenerator generator = new FileGenerator(destFolder + "/ApiRequests.java");
        generator.appendLn("package " + JavaConfig.PACKAGE + ";");
        generator.appendLn(JavaConfig.NOTICE);
        generator.appendLn();
        for (String im : JavaConfig.IMPORTS) {
            generator.appendLn("import " + im + ";");
        }
        generator.appendLn("import " + JavaConfig.PACKAGE + ".rpc.*;");
        generator.appendLn("import im.actor.api.*;");
        generator.appendLn("import com.droidkit.actors.concurrency.Future;");
        generator.appendLn("import com.droidkit.actors.concurrency.FutureCallback;");
        generator.appendLn("import java.util.concurrent.TimeoutException;");

        generator.appendLn();
        generator.appendLn("public class ApiRequests {");
        generator.increaseDepth();
        generator.appendLn("private ActorApi api;");
        generator.appendLn();
        generator.appendLn("public ApiRequests(ActorApi api) {");
        generator.increaseDepth();
        generator.appendLn("this.api = api;");
        generator.decreaseDepth();
        generator.appendLn("}");
        generator.appendLn();

        for (SchemeRpc u : definition.getAllRpc()) {
            String javaName = JavaConfig.getRequestName(u.getName());

            String responseJavaName;
            if (u.getResponse() instanceof SchemeRpc.RefResponse) {
                responseJavaName = JavaConfig.getResponseName(((SchemeRpc.RefResponse) u.getResponse()).getName());
            } else {
                responseJavaName = JavaConfig.getAnonymousResponseName(u.getName());
            }

            generator.append("public Future<" + responseJavaName + "> " + JavaConfig.getRequestsName(u.getName()) + "(");
            if (u.getAttributes().size() > 0) {
                ContainerGenerator.generateConstructorArgs(generator, definition, u);
            }
            generator.appendLn(") {");
            generator.increaseDepth();
            generator.append("return this.api.rpc(new " + javaName + "(");
            ContainerGenerator.generateConstructorArgsValues(generator, u);
            generator.appendLn("));");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();

            generator.append("public Future<" + responseJavaName + "> " + JavaConfig.getRequestsName(u.getName()) + "(");
            if (u.getAttributes().size() > 0) {
                ContainerGenerator.generateConstructorArgs(generator, definition, u);
                generator.append(", ");
            }
            generator.appendLn("long requestTimeout) {");
            generator.increaseDepth();
            generator.append("return this.api.rpc(new " + javaName + "(");
            ContainerGenerator.generateConstructorArgsValues(generator, u);
            generator.appendLn("), requestTimeout);");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();

            generator.append("public Future<" + responseJavaName + "> " + JavaConfig.getRequestsName(u.getName()) + "(");
            if (u.getAttributes().size() > 0) {
                ContainerGenerator.generateConstructorArgs(generator, definition, u);
                generator.append(", ");
            }
            generator.appendLn("FutureCallback<" + responseJavaName + "> callback) {");
            generator.increaseDepth();
            generator.append("return this.api.rpc(new " + javaName + "(");
            ContainerGenerator.generateConstructorArgsValues(generator, u);
            generator.appendLn("), callback);");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();

            generator.append("public Future<" + responseJavaName + "> " + JavaConfig.getRequestsName(u.getName()) + "(");
            if (u.getAttributes().size() > 0) {
                ContainerGenerator.generateConstructorArgs(generator, definition, u);
                generator.append(", ");
            }
            generator.appendLn("long requestTimeout, FutureCallback<" + responseJavaName + "> callback) {");
            generator.increaseDepth();
            generator.append("return this.api.rpc(new " + javaName + "(");
            ContainerGenerator.generateConstructorArgsValues(generator, u);
            generator.appendLn("), requestTimeout, callback);");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();

            generator.append("public " + responseJavaName + " " + JavaConfig.getRequestsName(u.getName()) + "Sync (");
            if (u.getAttributes().size() > 0) {
                ContainerGenerator.generateConstructorArgs(generator, definition, u);
            }
            generator.appendLn(") throws TimeoutException, ApiRequestException {");
            generator.increaseDepth();
            generator.append("return this.api.rpcSync(new " + javaName + "(");
            ContainerGenerator.generateConstructorArgsValues(generator, u);
            generator.appendLn("));");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();

            generator.append("public " + responseJavaName + " " + JavaConfig.getRequestsName(u.getName()) + "Sync (");
            if (u.getAttributes().size() > 0) {
                ContainerGenerator.generateConstructorArgs(generator, definition, u);
                generator.append(", ");
            }
            generator.appendLn("long requestTimeout) throws TimeoutException, ApiRequestException {");
            generator.increaseDepth();
            generator.append("return this.api.rpcSync(new " + javaName + "(");
            ContainerGenerator.generateConstructorArgsValues(generator, u);
            generator.appendLn("), requestTimeout);");
            generator.decreaseDepth();
            generator.appendLn("}");
            generator.appendLn();
        }

        generator.decreaseDepth();
        generator.appendLn("}");
        generator.close();
    }

    public static void generateParser(SchemeDefinition definition, String path) throws IOException {
        String destFolder = path + "/" + JavaConfig.PATH + "/parser/";
        new File(destFolder).mkdirs();
        FileGenerator generator = new FileGenerator(destFolder + "/RpcParser.java");
        generator.appendLn("package " + JavaConfig.PACKAGE + ".parser;");
        generator.appendLn(JavaConfig.NOTICE);
        generator.appendLn();
        for (String im : JavaConfig.IMPORTS) {
            generator.appendLn("import " + im + ";");
        }
        generator.appendLn("import " + JavaConfig.PACKAGE + ".rpc.*;");
        generator.appendLn("import " + JavaConfig.PACKAGE + ".base.*;");
        generator.appendLn();
        generator.appendLn("public class RpcParser extends BaseParser<RpcScope> {");
        generator.increaseDepth();
        generator.appendLn("@Override");
        generator.appendLn("public RpcScope read(int type, byte[] payload) throws IOException {");
        generator.increaseDepth();
        generator.appendLn("switch(type) {");
        generator.increaseDepth();
        for (SchemeRpc u : definition.getAllRpc()) {
            String javaName = JavaConfig.getRequestName(u.getName());
            generator.appendLn("case " + u.getHeader() + ": return " + javaName + ".fromBytes(payload);");
        }
        for (SchemeBaseResponse u : definition.getAllResponses()) {
            String javaName;
            if (u instanceof SchemeResponse) {
                javaName = JavaConfig.getResponseName(((SchemeResponse) u).getName());
            } else {
                javaName = JavaConfig.getAnonymousResponseName(((SchemeResponseAnonymous) u).getRpc().getName());
            }
            generator.appendLn("case " + u.getHeader() + ": return " + javaName + ".fromBytes(payload);");
        }
        for (SchemeUpdateBox u : definition.getAllUpdateBoxes()) {
            String javaName = JavaConfig.getUpdateBoxName(u.getName());
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