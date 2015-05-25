/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.api.DocumentExVideo;
import im.actor.model.api.DocumentMessage;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.content.internal.ContentLocalContainer;
import im.actor.model.entity.content.internal.ContentRemoteContainer;
import im.actor.model.entity.content.internal.LocalFastThumb;
import im.actor.model.entity.content.internal.LocalVideo;

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

    public static PhotoContent createRemotePhoto(FileReference reference, int w, int h,
                                                 int duration, FastThumb fastThumb) {
        return new PhotoContent(new ContentRemoteContainer(
                new DocumentMessage(reference.getFileId(),
                        reference.getAccessHash(),
                        reference.getFileSize(),
                        reference.getFileName(),
                        "video/mp4",
                        fastThumb != null ?
                                new im.actor.model.api.FastThumb(
                                        fastThumb.getW(),
                                        fastThumb.getH(),
                                        fastThumb.getImage()) :
                                null,
                        new DocumentExVideo(w, h, duration))));
    }

    private int duration;
    private int w;
    private int h;

    public VideoContent(ContentRemoteContainer contentContainer) {
        super(contentContainer);
        DocumentExVideo video =
                (DocumentExVideo) ((DocumentMessage) contentContainer.getMessage()).getExt();
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
