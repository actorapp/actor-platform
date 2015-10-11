/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

public class JsContentDocument extends JsContent {

    public native static JsContentDocument create(String fileName, String fileExtension, String fileSize, String preview, String fileUrl, boolean isUploading)/*-{
        return {content: "document", fileName: fileName, fileExtension: fileExtension, fileSize: fileSize, preview: preview, fileUrl: fileUrl, isUploading: isUploading};
    }-*/;

    protected JsContentDocument() {

    }
}
