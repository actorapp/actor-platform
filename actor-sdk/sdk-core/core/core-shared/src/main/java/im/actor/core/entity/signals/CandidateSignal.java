package im.actor.core.entity.signals;

import java.io.IOException;

import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class CandidateSignal extends AbsSignal {

    private String sdp;
    private int label;
    private String id;

    public CandidateSignal(String id, int label, String sdp) {
        this.sdp = sdp;
        this.id = id;
        this.label = label;

    }

    public CandidateSignal() {
    }

    public String getSdp() {
        return sdp;
    }

    public int getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }

    @Override
    public void parseSignal(BserValues values) throws IOException {
        this.sdp = values.getString(2);
        this.id = values.getString(3);
        this.label = values.getInt(4);
    }

    @Override
    public void serializeSignal(BserWriter writer) throws IOException {
        writer.writeString(2, sdp);
        writer.writeString(3, id);
        writer.writeInt(4, label);
    }

    @Override
    public String getType() {
        return "candidate";
    }
}
