/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import im.actor.core.api.ApiDocumentExPhoto;
import im.actor.core.api.ApiDocumentExVoice;
import im.actor.core.api.ApiDocumentMessage;
import im.actor.core.api.ApiFastThumb;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.content.internal.ContentLocalContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.core.entity.content.internal.LocalFastThumb;
import im.actor.core.entity.content.internal.LocalPhoto;
import im.actor.core.entity.content.internal.LocalVoice;

public class VoiceContent extends DocumentContent {

    @NotNull
    public static VoiceContent createLocalAudio(@NotNull String descriptor, @NotNull String fileName, int fileSize,
                                                int duration) {
        return new VoiceContent(new ContentLocalContainer(
                new LocalVoice(
                        fileName,
                        descriptor,
                        fileSize,
                        "audio/mp3",
                        duration)));
    }

    @NotNull
    public static VoiceContent createRemoteAudio(@NotNull FileReference reference, int duration) {
        return new VoiceContent(new ContentRemoteContainer(
                new ApiDocumentMessage(reference.getFileId(),
                        reference.getAccessHash(),
                        reference.getFileSize(),
                        reference.getFileName(),
                        "audio/mp3",
                        null,
                        new ApiDocumentExVoice(duration))));
    }

    private int duration;

    public VoiceContent(ContentLocalContainer contentLocalContainer) {
        super(contentLocalContainer);
        LocalVoice photo = ((LocalVoice) contentLocalContainer.getContent());
        duration = photo.getDuration();
    }

    public VoiceContent(ContentRemoteContainer contentRemoteContainer) {
        super(contentRemoteContainer);
        ApiDocumentMessage message = (ApiDocumentMessage) contentRemoteContainer.getMessage();
        ApiDocumentExVoice photo = (ApiDocumentExVoice) message.getExt();
        if (photo != null) {
            duration = photo.getDuration();
        }
    }

    public int getDuration() {
        return duration;
    }
}
