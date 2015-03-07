package im.actor.model.modules.messages.encrypted;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class EncryptedTextMessage extends BserObject {

    public static EncryptedTextMessage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new EncryptedTextMessage(), data);
    }

    private String text;
    private int extType;
    private byte[] extension;

    public EncryptedTextMessage(String text, int extType, byte[] extension) {
        this.text = text;
        this.extType = extType;
        this.extension = extension;
    }

    private EncryptedTextMessage() {

    }

    public String getText() {
        return text;
    }

    public int getExtType() {
        return extType;
    }

    public byte[] getExtension() {
        return extension;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        text = values.getString(1);
        extType = values.getInt(2);
        extension = values.optBytes(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(1, text);
        writer.writeInt(2, extType);
        if (extension != null) {
            writer.writeBytes(3, extension);
        }
    }
}
