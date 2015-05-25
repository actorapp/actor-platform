/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.DocumentMessage;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.content.internal.AbsContentContainer;
import im.actor.model.entity.content.internal.ContentLocalContainer;
import im.actor.model.entity.content.internal.ContentRemoteContainer;
import im.actor.model.entity.content.internal.LocalDocument;

public class ObsoleteDocument extends BserObject {

    protected ObsoleteFileSource source;
    protected String mimeType;
    protected String name;
    protected ObsoleteFastThumb fastThumb;

    public ObsoleteDocument(BserValues values) throws IOException {
        parse(values);
    }

    public AbsContentContainer toContainer() {
        if (source instanceof ObsoleteLocalFileSource) {
            ObsoleteLocalFileSource fSource = (ObsoleteLocalFileSource) source;
            return new ContentLocalContainer(new LocalDocument(
                    name,
                    fSource.getFileDescriptor(),
                    fSource.getSize(),
                    mimeType,
                    fastThumb != null ? fastThumb.toFastThumb() : null));
        } else if (source instanceof ObsoleteRemoteFileSource) {
            ObsoleteRemoteFileSource fSource = (ObsoleteRemoteFileSource) source;
            return new ContentRemoteContainer(new DocumentMessage(
                    fSource.getFileReference().getFileId(),
                    fSource.getFileReference().getAccessHash(),
                    fSource.getFileReference().getFileSize(),
                    name,
                    mimeType,
                    fastThumb != null ? fastThumb.toApiFastThumb() : null,
                    null));
        } else {
            throw new RuntimeException("Unknwon source type");
        }
    }

    public ObsoleteFileSource getSource() {
        return source;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getName() {
        return name;
    }

    public ObsoleteFastThumb getFastThumb() {
        return fastThumb;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        source = ObsoleteFileSource.fromBytes(values.getBytes(2));
        mimeType = values.getString(3);
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
}
