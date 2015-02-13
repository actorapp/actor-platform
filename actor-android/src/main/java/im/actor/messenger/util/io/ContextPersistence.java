package im.actor.messenger.util.io;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ContextPersistence extends PersistenceObject {

    protected transient Context context;

    public ContextPersistence(Context context) {
        this.context = context;
    }

    @Override
    protected OutputStream openWrite(String path) throws FileNotFoundException, FileNotFoundException {
        return context.openFileOutput(path, Context.MODE_PRIVATE);
    }

    @Override
    protected InputStream openRead(String path, boolean error) throws IOException {
        if (error) {
            return context.getAssets().open(path);
        } else {
            return context.openFileInput(path);
        }
    }
}