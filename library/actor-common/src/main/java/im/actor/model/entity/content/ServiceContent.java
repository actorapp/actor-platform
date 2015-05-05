/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ServiceContent extends AbsContent {

    public static ServiceContent serviceFromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceContent(), data);
    }

    private String compatText;

    public ServiceContent(String compatText) {
        this.compatText = compatText;
    }

    protected ServiceContent() {

    }

    public String getCompatText() {
        return compatText;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.SERVICE;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        compatText = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeString(2, compatText);
    }
}