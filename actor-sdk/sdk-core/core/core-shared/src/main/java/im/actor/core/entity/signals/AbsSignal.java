package im.actor.core.entity.signals;

import java.io.IOException;

import im.actor.runtime.Log;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.bser.DataInput;

public abstract class AbsSignal extends BserObject {

    String type;

    @Override
    public void parse(BserValues values) throws IOException {
        type = values.getString(1);
        parseSignal(values);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(1, getType());
        serializeSignal(writer);
    }

    public static AbsSignal fromBytes(byte[] data) {
        try {

            AbsSignal res = null;
            BserValues values = new BserValues(BserParser.deserialize(new DataInput(data, 0, data.length)));
            if (values.getString(1).equals(new OfferSignal().getType())) {
                res = new OfferSignal();
            } else if (values.getString(1).equals(new AnswerSignal().getType())) {
                res = new AnswerSignal();
            } else if (values.getString(1).equals(new CandidateSignal().getType())) {
                res = new CandidateSignal();
            }

            if (res != null) {
                res.parse(values);
            } else {
                Log.w("Signaling parser", "unknown signal");
            }
            return res;
        } catch (IOException e) {
            return null;
        }
    }

    public abstract String getType();

    protected abstract void parseSignal(BserValues values) throws IOException;

    protected abstract void serializeSignal(BserWriter writer) throws IOException;

}
