package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class TextContent extends AbsContent {

    public static TextContent textFromBytes(byte[] data) throws IOException {
        return Bser.parse(new TextContent(), data);
    }

    private String text;

    public TextContent(String text) {
        this.text = text;
    }

    private TextContent() {

    }

    public String getText() {
        return text;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.TEXT;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        text = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(2, text);
    }
}
