/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

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
        super.parse(values);
        text = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(2, text);
    }
}
