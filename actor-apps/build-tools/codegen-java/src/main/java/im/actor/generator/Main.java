package im.actor.generator;

import im.actor.generator.generators.ProtoBufGenerator;
import im.actor.generator.generators.doc.DocIndexGenerator;
import im.actor.generator.generators.doc.DocUpdatesGenerator;
import im.actor.generator.generators.java.*;
import im.actor.generator.scheme.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Actor Api scheme serialization generator");
        if (args.length < 2) {
            System.out.println("No file specified. Exiting.");
            return;
        }

        System.out.println("Reading schema from file...");
        String workingDir = new File(args[0]).getParent();
        SchemeDefinition definition = SchemeFactory.fromFile(args[0]);

        System.out.println("Generating java files...");
        String destJava = args[1];

        EnumGenerator.generate(definition, destJava);
        UpdateGenerator.generate(definition, destJava);
        UpdateGenerator.generateParser(definition, destJava);
        StructGenerator.generate(definition, destJava);
        RequestGenerator.generate(definition, destJava);
        RequestGenerator.generateParser(definition, destJava);
        UpdateBoxGenerator.generate(definition, destJava);
        MetaGenerator.generate(definition, destJava);

        System.out.println("Success");
    }
}
