package im.actor.generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by ex3ndr on 14.11.14.
 */
public class FileGenerator {
    private int depth = 0;
    private OutputStreamWriter stream;
    private boolean isFirstData;

    public FileGenerator(String name) throws IOException {
        stream = new OutputStreamWriter(new FileOutputStream(name));
    }

    public void increaseDepth() {
        depth++;
    }

    public void decreaseDepth() {
        depth--;
        if (depth < 0) {
            depth = 0;
        }
    }

    public void append(String value) throws IOException {
        if (isFirstData) {
            String padding = "";
            for (int i = 0; i < depth; i++) {
                padding += "    ";
            }
            stream.append(padding);
            isFirstData = false;
        }
        stream.append(value);
    }

    public void appendLn(String value) throws IOException {
        append(value);
        stream.append("\n");
        isFirstData = true;
    }

    public void appendLn() throws IOException {
        stream.append("\n");
        isFirstData = true;
    }

    public void close() throws IOException {
        stream.close();
    }
}
