/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.Message;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

public class ObsoleteDocumentContent extends ObsoleteAbsContent {

    String mimetype;
    String name;
    ObsoleteFastThumb fastThumb;

    @Override
    public void parse(BserValues values) throws IOException {
        //    source = FileSource.fromBytes(values.getBytes(2));
        mimetype = values.getString(3);
        name = values.getString(4);
        byte[] ft = values.optBytes(5);
        if (ft != null) {
            fastThumb = new ObsoleteFastThumb(ft);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message toApiMessage() {
        return null;
    }
}
