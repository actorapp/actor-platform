/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiDocumentMessage;
import im.actor.core.api.ApiFastThumb;
import im.actor.core.api.ApiFileLocation;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.content.internal.ContentLocalContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.core.entity.content.internal.LocalDocument;
import im.actor.core.entity.content.internal.LocalFastThumb;

public class DocumentContent extends AbsContent {

    public static DocumentContent createLocal(String fileName, int fileSize, String descriptor,
                                              String mimeType, FastThumb fastThumb) {
        return new DocumentContent(new ContentLocalContainer(new LocalDocument(fileName,
                descriptor, fileSize, mimeType,
                fastThumb != null ? new LocalFastThumb(fastThumb) : null)));
    }

    public static DocumentContent createRemoteDocument(FileReference reference, FastThumb fastThumb) {
        return new DocumentContent(new ContentRemoteContainer(
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
                        null)));
    }

    protected FileSource source;
    protected String mimeType;
    protected String name;
    protected FastThumb fastThumb;

    public DocumentContent(ContentRemoteContainer contentContainer) {
        super(contentContainer);
        ApiDocumentMessage doc = ((ApiDocumentMessage) contentContainer.getMessage());
        source = new FileRemoteSource(new FileReference(
                new ApiFileLocation(doc.getFileId(), doc.getAccessHash()), doc.getName(),
                doc.getFileSize()));
        mimeType = doc.getMimeType();
        name = doc.getName();
        fastThumb = doc.getThumb() != null ? new FastThumb(doc.getThumb()) : null;
    }

    public DocumentContent(ContentLocalContainer contentContainer) {
        super(contentContainer);
        LocalDocument localDocument = (LocalDocument) contentContainer.getContent();
        source = new FileLocalSource(localDocument.getFileName(),
                localDocument.getFileSize(), localDocument.getFileDescriptor());
        mimeType = localDocument.getMimeType();
        name = localDocument.getFileName();
        fastThumb = localDocument.getFastThumb() != null ? new FastThumb(localDocument.getFastThumb()) : null;
    }

    public FileSource getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public FastThumb getFastThumb() {
        return fastThumb;
    }

    public String getExt() {
        String ext = "";
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex >= 0) {
            ext = name.substring(dotIndex + 1);
        }
        return ext;
    }

    public String getMimeType() {
        return mimeType;
    }
}
