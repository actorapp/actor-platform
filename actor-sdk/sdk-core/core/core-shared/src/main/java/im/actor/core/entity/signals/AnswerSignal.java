package im.actor.core.entity.signals;

import java.io.IOException;

import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

/**
 * Created by root on 1/15/16.
 */
public class AnswerSignal extends AbsSignal {

    private String sdp;

    public AnswerSignal(String sdp) {
        this.sdp = sdp;
    }

    public AnswerSignal() {
    }

    public String getSdp() {
        return sdp;
    }

    @Override
    public void parseSignal(BserValues values) throws IOException {
        this.sdp = values.getString(2);
    }

    @Override
    public void serializeSignal(BserWriter writer) throws IOException {
        writer.writeString(2, sdp);
    }

    @Override
    public String getType() {
        return "answer";
    }
}
