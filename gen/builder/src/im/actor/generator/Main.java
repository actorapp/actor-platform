package im.actor.generator;

import im.actor.generator.generators.ProtoBufGenerator;
import im.actor.generator.generators.doc.DocIndexGenerator;
import im.actor.generator.generators.doc.DocUpdatesGenerator;
import im.actor.generator.generators.java.*;
import im.actor.generator.scheme.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SchemeDefinition definition = SchemeFactory.fromFile("/Users/ex3ndr/Develop/actor-api-schema/actor.json");
//        SchemeDefinition encryptedDefinition = SchemeFactory.fromFile("/Users/ex3ndr/Documents/actor_encrypted.json");

        String destJava ="/Users/ex3ndr/Develop/actor-api-schema/java/";
        EnumGenerator.generate(definition, destJava);
        UpdateGenerator.generate(definition, destJava);
        UpdateGenerator.generateParser(definition, destJava);
        StructGenerator.generate(definition,destJava);
        RequestGenerator.generate(definition, destJava);
        RequestGenerator.generateParser(definition, destJava);
        RequestGenerator.generateRpcList(definition, destJava);
        UpdateBoxGenerator.generate(definition, destJava);

        //        DocIndexGenerator.generate(definition, "/Users/ex3ndr/actor-doc/");
//        DocUpdatesGenerator.generate(definition, "/Users/ex3ndr/actor-doc/");

        // EnumGenerator.generate(encryptedDefinition, "/Users/ex3ndr/Documents/actor/java/", JavaConfig.PACKAGE + ".encrypted");
        // StructGenerator.generate(encryptedDefinition, "/Users/ex3ndr/Documents/actor/java/", JavaConfig.PACKAGE + ".encrypted");

//        ProtoBufGenerator.generate(encryptedDefinition, "/Users/ex3ndr/actor-doc/actor_encrypted.proto");
        ProtoBufGenerator.generate(definition, "/Users/ex3ndr/Develop/actor-api-schema/actor.proto");
    }
}
