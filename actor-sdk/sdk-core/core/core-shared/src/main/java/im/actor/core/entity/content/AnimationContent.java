package im.actor.core.entity.content;

import im.actor.core.api.ApiDocumentExAnimation;
import im.actor.core.api.ApiDocumentMessage;
import im.actor.core.api.ApiFastThumb;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.content.internal.ContentLocalContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.core.entity.content.internal.LocalAnimation;
import im.actor.core.entity.content.internal.LocalFastThumb;
import im.actor.core.entity.content.internal.LocalVideo;

public class AnimationContent extends DocumentContent {

    public static AnimationContent createLocalAnimation(String descriptor, String fileName, int fileSize,
                                                        int w, int h, FastThumb fastThumb) {
        return new AnimationContent(new ContentLocalContainer(
                new LocalAnimation(
                        fileName,
                        descriptor,
                        fileSize,
                        "image/gif",
                        fastThumb != null ? new LocalFastThumb(fastThumb) : null,
                        w, h)));
    }

    public static AnimationContent createRemoteAnimation(FileReference reference, int w, int h,
                                                         FastThumb fastThumb) {
        return new AnimationContent(new ContentRemoteContainer(
                new ApiDocumentMessage(reference.getFileId(),
                        reference.getAccessHash(),
                        reference.getFileSize(),
                        reference.getFileName(),
                        "image/gif",
                        fastThumb != null ?
                                new ApiFastThumb(
                                        fastThumb.getW(),
                                        fastThumb.getH(),
                                        fastThumb.getImage()) :
                                null,
                        new ApiDocumentExAnimation(w, h))));
    }

    private int w;
    private int h;

    public AnimationContent(ContentRemoteContainer contentContainer) {
        super(contentContainer);
        ApiDocumentExAnimation animation =
                (ApiDocumentExAnimation) ((ApiDocumentMessage) contentContainer.getMessage()).getExt();
        w = animation.getW();
        h = animation.getH();
    }

    public AnimationContent(ContentLocalContainer contentContainer) {
        super(contentContainer);
        LocalAnimation localVideo = (LocalAnimation) contentContainer.getContent();
        w = localVideo.getW();
        h = localVideo.getH();
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}
