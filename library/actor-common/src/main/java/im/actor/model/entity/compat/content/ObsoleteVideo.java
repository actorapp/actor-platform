/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.DocumentExVideo;
import im.actor.model.api.DocumentMessage;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.content.internal.AbsContentContainer;
import im.actor.model.entity.content.internal.ContentLocalContainer;
import im.actor.model.entity.content.internal.ContentRemoteContainer;
import im.actor.model.entity.content.internal.LocalVideo;

public class ObsoleteVideo extends ObsoleteDocument {
    private int w;
    private int h;
    private int duration;

    public ObsoleteVideo(BserValues values) throws IOException {
        super(values);
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public AbsContentContainer toContainer() {
        if (source instanceof ObsoleteLocalFileSource) {
            ObsoleteLocalFileSource fSource = (ObsoleteLocalFileSource) source;
            return new ContentLocalContainer(new LocalVideo(
                    name,
                    fSource.getFileDescriptor(),
                    fSource.getSize(),
                    mimeType,
                    fastThumb != null ? fastThumb.toFastThumb() : null,
                    w,
                    h,
                    duration));
        } else if (source instanceof ObsoleteRemoteFileSource) {
            ObsoleteRemoteFileSource fSource = (ObsoleteRemoteFileSource) source;
            return new ContentRemoteContainer(new DocumentMessage(
                    fSource.getFileReference().getFileId(),
                    fSource.getFileReference().getAccessHash(),
                    fSource.getFileReference().getFileSize(),
                    name,
                    mimeType,
                    fastThumb != null ? fastThumb.toApiFastThumb() : null,
                    new DocumentExVideo(w, h, duration)));
        } else {
            throw new RuntimeException("Unknwon source type");
        }
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        w = values.getInt(10);
        h = values.getInt(11);
        duration = values.getInt(12);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
}
