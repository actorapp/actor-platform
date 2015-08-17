/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.compat.content;

import java.io.IOException;

import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.core.entity.compat.ObsoleteFileReference;

public class ObsoleteRemoteFileSource extends ObsoleteFileSource {

    private ObsoleteFileReference fileReference;

    public ObsoleteRemoteFileSource(BserValues values) throws IOException {
        parse(values);
    }

    public ObsoleteFileReference getFileReference() {
        return fileReference;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        fileReference = new ObsoleteFileReference(values.getBytes(2));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
