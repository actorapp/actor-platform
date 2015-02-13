package im.actor.messenger.storage.scheme.state;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class SequenceState extends BserObject {
    private int seq;
    private byte[] state;

    public SequenceState(int seq, byte[] state) {
        this.seq = seq;
        this.state = state;
    }

    public SequenceState() {

    }

    public int getSeq() {
        return seq;
    }

    public byte[] getState() {
        return state;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        seq = values.getInt(1);
        state = values.getBytes(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, seq);
        writer.writeBytes(2, state);
    }
}
