package im.actor.messenger.storage.scheme.messages.types;

import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class TextMessage extends AbsMessage {
    private String text;
    private String formattedText;

    public TextMessage(String text, String formattedText, boolean isEncrypted) {
        super(isEncrypted);
        this.text = text;
        this.formattedText = formattedText;
    }

    public TextMessage() {
    }

    public String getText() {
        return text;
    }

    public String getFormattedText() {
        return formattedText;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        text = values.getString(1);
        formattedText = values.getString(2);
        isEncrypted = values.getBool(10, true);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(1, text);
        writer.writeString(2, formattedText);
        writer.writeBool(10, isEncrypted);
    }
}
