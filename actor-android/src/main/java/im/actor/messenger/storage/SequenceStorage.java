package im.actor.messenger.storage;

import android.content.Context;

import com.droidkit.bser.Bser;
import im.actor.messenger.storage.scheme.state.SequenceState;
import im.actor.messenger.util.io.SafeFileWriter;
import im.actor.messenger.util.Logger;

import java.io.*;

public class SequenceStorage {

    private static final String TAG = "StatePersistence";

    private SafeFileWriter writer;

    private SequenceState state;

    public SequenceStorage(Context context) {
        this.writer = new SafeFileWriter(context, "state.bin");

        byte[] data = writer.loadData();
        if (data != null) {
            try {
                state = Bser.parse(SequenceState.class, data);
            } catch (IOException e) {
                Logger.d(TAG, "", e);
            }
        }

        if (state == null) {
            try {
                state = new SequenceState(-1, new byte[0]);
            } catch (Exception e1) {
                throw new RuntimeException("Unable to instantiate default settings");
            }
        }
    }

    public synchronized void setState(final int seq, final byte[] state) {
        this.state = new SequenceState(seq, state);
        write();
    }

    public synchronized SequenceState getState() {
        return state;
    }

    public synchronized void dropState() {
        state = new SequenceState(-1, new byte[0]);
        writer.remove();
    }

    private void write() {
        writer.saveData(state.toByteArray());
    }
}
