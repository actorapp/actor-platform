/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

public class JsContentSticker extends JsContent {
    public native static JsContentSticker create(String fileName, String fileExtension, String fileSize, int w, int h, String preview, String fileUrl, boolean isUploading)/*-{
        return {content: "sticker", fileName: fileName, fileExtension: fileExtension, w: w, h: h, preview: preview, fileSize: fileSize, fileUrl: fileUrl, isUploading: isUploading};
    }-*/;

    protected JsContentSticker() {

    }
}
