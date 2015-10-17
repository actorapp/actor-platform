/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiDocumentExVideo;
import im.actor.core.api.ApiDocumentMessage;
import im.actor.core.api.ApiFastThumb;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.content.internal.ContentLocalContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.core.entity.content.internal.LocalFastThumb;
import im.actor.core.entity.content.internal.LocalVideo;

public class VideoContent extends DocumentContent {

    public static VideoContent createLocalVideo(String descriptor, String fileName, int fileSize,
                                                int w, int h, int duration, FastThumb fastThumb) {
        return new VideoContent(new ContentLocalContainer(
                new LocalVideo(
                        fileName,
                        descriptor,
                        fileSize,
                        "video/mp4",
                        fastThumb != null ? new LocalFastThumb(fastThumb) : null,
                        w, h, duration)));
    }

    public static VideoContent createRemoteVideo(FileReference reference, int w, int h,
                                                 int duration, FastThumb fastThumb) {
        return new VideoContent(new ContentRemoteContainer(
                new ApiDocumentMessage(reference.getFileId(),
                        reference.getAccessHash(),
                        reference.getFileSize(),
                        reference.getFileName(),
                        "video/mp4",
                        fastThumb != null ?
                                new ApiFastThumb(
                                        fastThumb.getW(),
                                        fastThumb.getH(),
                                        fastThumb.getImage()) :
                                null,
                        new ApiDocumentExVideo(w, h, duration))));
    }

    private int duration;
    private int w;
    private int h;

    public VideoContent(ContentRemoteContainer contentContainer) {
        super(contentContainer);
        ApiDocumentExVideo video =
                (ApiDocumentExVideo) ((ApiDocumentMessage) contentContainer.getMessage()).getExt();
        w = video.getW();
        h = video.getH();
        duration = video.getDuration();
    }

    public VideoContent(ContentLocalContainer contentContainer) {
        super(contentContainer);
        LocalVideo localVideo = (LocalVideo) contentContainer.getContent();
        w = localVideo.getW();
        h = localVideo.getH();
        duration = localVideo.getDuration();
    }

    public int getDuration() {
        return duration;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}
