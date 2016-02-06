package im.actor.core.entity.signals;

import java.io.IOException;

import im.actor.runtime.Log;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserParser;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.bser.DataInput;

public abstract class AbsSignal extends BserObject {

    private String type;

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
            BserValues values = new BserValues(BserParser.deserialize(new DataInput(data, 0, data.length)));
            String type = values.getString(1);
            AbsSignal res;
            if (OfferSignal.TYPE.equals(type)) {
                res = new OfferSignal();
            } else if (AnswerSignal.TYPE.equals(type)) {
                res = new AnswerSignal();
            } else if (CandidateSignal.TYPE.equals(type)) {
                res = new CandidateSignal();
            } else {
                throw new IOException("Unknown signal type " + type);
            }
            res.parse(values);
            return res;
        } catch (IOException e) {
            return null;
        }
    }

    public abstract String getType();

    protected abstract void parseSignal(BserValues values) throws IOException;

    protected abstract void serializeSignal(BserWriter writer) throws IOException;

}
