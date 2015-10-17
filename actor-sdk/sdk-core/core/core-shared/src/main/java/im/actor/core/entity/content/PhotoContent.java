/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import im.actor.core.api.ApiDocumentExPhoto;
import im.actor.core.api.ApiDocumentMessage;
import im.actor.core.api.ApiFastThumb;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.content.internal.ContentLocalContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.core.entity.content.internal.LocalFastThumb;
import im.actor.core.entity.content.internal.LocalPhoto;

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
                new ApiDocumentMessage(reference.getFileId(),
                        reference.getAccessHash(),
                        reference.getFileSize(),
                        reference.getFileName(),
                        "image/jpeg",
                        fastThumb != null ?
                                new ApiFastThumb(
                                        fastThumb.getW(),
                                        fastThumb.getH(),
                                        fastThumb.getImage()) :
                                null,
                        new ApiDocumentExPhoto(w, h))));
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
        ApiDocumentMessage message = (ApiDocumentMessage) contentRemoteContainer.getMessage();
        ApiDocumentExPhoto photo = (ApiDocumentExPhoto) message.getExt();
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
