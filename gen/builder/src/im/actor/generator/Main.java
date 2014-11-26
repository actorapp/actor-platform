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
//        EnumGenerator.generate(definition, "/Users/ex3ndr/Documents/actor/java/");
//        EnumGenerator.generate(encryptedDefinition, "/Users/ex3ndr/Documents/actor/java/", JavaConfig.PACKAGE + ".encrypted");
//        UpdateGenerator.generate(definition, "/Users/ex3ndr/Documents/actor/java/");
//        UpdateGenerator.generateParser(definition, "/Users/ex3ndr/Documents/actor/java/");
//        StructGenerator.generate(definition, "/Users/ex3ndr/Documents/actor/java/");
//        StructGenerator.generate(encryptedDefinition, "/Users/ex3ndr/Documents/actor/java/", JavaConfig.PACKAGE + ".encrypted");
//        RequestGenerator.generate(definition, "/Users/ex3ndr/Documents/actor/java/");
//        RequestGenerator.generateParser(definition, "/Users/ex3ndr/Documents/actor/java/");
//        RequestGenerator.generateRpcList(definition, "/Users/ex3ndr/Documents/actor/java/");
//        UpdateBoxGenerator.generate(definition, "/Users/ex3ndr/Documents/actor/java/");
//
//        ProtoBufGenerator.generate(encryptedDefinition, "/Users/ex3ndr/Documents/actor_encrypted.proto");
//        ProtoBufGenerator.generate(definition, "/Users/ex3ndr/Documents/actor.proto");

//        DocIndexGenerator.generate(definition, "/Users/ex3ndr/actor-doc/");
//        DocUpdatesGenerator.generate(definition, "/Users/ex3ndr/actor-doc/");
//        ProtoBufGenerator.generate(encryptedDefinition, "/Users/ex3ndr/actor-doc/actor_encrypted.proto");
        ProtoBufGenerator.generate(definition, "/Users/ex3ndr/Develop/actor-api-schema/actor.proto");
    }
}
