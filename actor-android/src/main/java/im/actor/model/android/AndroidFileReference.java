package im.actor.model.android;

import java.io.File;
import java.io.IOException;

import im.actor.model.files.FileReference;
import im.actor.model.files.InputFile;
import im.actor.model.files.OutputFile;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class AndroidFileReference implements FileReference {
    private String fileName;

    public AndroidFileReference(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getDescriptor() {
        return fileName;
    }

    @Override
    public boolean isExist() {
        return new File(fileName).exists();
    }

    @Override
    public int getSize() {
        return (int) new File(fileName).length();
    }

    @Override
    public OutputFile openWrite(int size) {
        try {
            return new AndroidOutputFile(fileName, size);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public InputFile openRead() {
        return null;
    }
}
