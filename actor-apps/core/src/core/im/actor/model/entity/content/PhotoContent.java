/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import im.actor.model.api.DocumentExPhoto;
import im.actor.model.api.DocumentMessage;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.content.internal.ContentLocalContainer;
import im.actor.model.entity.content.internal.ContentRemoteContainer;
import im.actor.model.entity.content.internal.LocalFastThumb;
import im.actor.model.entity.content.internal.LocalPhoto;

public class PhotoContent extends DocumentContent {

    @NotNull
    public static PhotoContent createLocalPhoto(@NotNull String descriptor, @NotNull String fileName, int fileSize,
                                                int w, int h, @Nullable FastThumb fastThumb) {
        return new PhotoContent(new ContentLocalContainer(
                new LocalPhoto(
                        fileName,
                        descriptor,
                        fileSize,
                        "image/jpeg",
                        fastThumb != null ? new LocalFastThumb(fastThumb) : null,
                        w, h)));
    }

    @NotNull
    public static PhotoContent createRemotePhoto(@NotNull FileReference reference, int w, int h,
                                                 @Nullable FastThumb fastThumb) {
        return new PhotoContent(new ContentRemoteContainer(
                new DocumentMessage(reference.getFileId(),
                        reference.getAccessHash(),
                        reference.getFileSize(),
                        reference.getFileName(),
                        "image/jpeg",
                        fastThumb != null ?
                                new im.actor.model.api.FastThumb(
                                        fastThumb.getW(),
                                        fastThumb.getH(),
                                        fastThumb.getImage()) :
                                null,
                        new DocumentExPhoto(w, h))));
    }

    private int w;
    private int h;

    public PhotoContent(ContentLocalContainer contentLocalContainer) {
        super(contentLocalContainer);
        LocalPhoto photo = ((LocalPhoto) contentLocalContainer.getContent());
        w = photo.getW();
        h = photo.getH();
    }

    public PhotoContent(ContentRemoteContainer contentRemoteContainer) {
        super(contentRemoteContainer);
        DocumentMessage message = (DocumentMessage) contentRemoteContainer.getMessage();
        DocumentExPhoto photo = (DocumentExPhoto) message.getExt();
        if (photo != null) {
            w = photo.getW();
            h = photo.getH();
        }
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}
