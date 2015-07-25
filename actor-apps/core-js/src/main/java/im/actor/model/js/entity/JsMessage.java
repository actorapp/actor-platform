/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import com.google.gwt.core.client.JsDate;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FileLocalSource;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.ServiceContent;
import im.actor.model.entity.content.TextContent;
import im.actor.model.js.JsMessenger;
import im.actor.model.util.Base64Utils;

public class JsMessage extends JavaScriptObject {

    public static final JsEntityConverter<Message, JsMessage> CONVERTER = new JsEntityConverter<Message, JsMessage>() {
        @Override
        public JsMessage convert(Message value, JsMessenger modules) {

            String rid = value.getRid() + "";
            String sortKey = value.getSortDate() + "";

            JsPeerInfo sender = modules.buildPeerInfo(Peer.user(value.getSenderId()));
            boolean isOut = value.getSenderId() == modules.myUid();
            String date = modules.getFormatter().formatTime(value.getDate());
            JsDate fullDate = JsDate.create(value.getDate());

            JsContent content;
            if (value.getContent() instanceof TextContent) {
                String text = ((TextContent) value.getContent()).getText();
//                text = SafeHtmlUtils.htmlEscape(text).replace("\n", "<br />");
//                content = JsContentText.create(text);
                content = JsContentText.create(text);
            } else if (value.getContent() instanceof ServiceContent) {
                content = JsContentService.create(modules.getFormatter().formatFullServiceMessage(value.getSenderId(), (ServiceContent) value.getContent()));
            } else if (value.getContent() instanceof DocumentContent) {
                DocumentContent doc = (DocumentContent) value.getContent();

                String fileName = doc.getName();
                String fileExtension = doc.getExt();
                String fileSize = modules.getFormatter().formatFileSize(doc.getSource().getSize());
                String fileUrl = null;

                if (doc.getSource() instanceof FileRemoteSource) {
                    fileUrl = modules.getFileUrl(((FileRemoteSource) doc.getSource()).getFileReference());
                }

                boolean isUploading = doc.getSource() instanceof FileLocalSource;

                String thumb = null;
                if (doc.getFastThumb() != null) {
                    String thumbBase64 = Base64Utils.toBase64(doc.getFastThumb().getImage());
                    thumb = "data:image/jpg;base64," + thumbBase64;
                }

                if (value.getContent() instanceof PhotoContent && thumb != null) {
                    PhotoContent photoContent = (PhotoContent) value.getContent();
                    content = JsContentPhoto.create(
                            fileName, fileExtension, fileSize,
                            photoContent.getW(), photoContent.getH(), thumb,
                            fileUrl, isUploading);
                } else {
                    content = JsContentDocument.create(fileName, fileExtension, fileSize,
                            thumb, fileUrl, isUploading);
                }

            } else {
                content = JsContentUnsupported.create();
            }

            return create(rid, sortKey, sender, isOut, date, fullDate, Enums.convert(value.getMessageState()), content);
        }
    };

    public native static JsMessage create(String rid, String sortKey, JsPeerInfo sender, boolean isOut, String date, JsDate fullDate, String state, JsContent content)/*-{
        return {
            rid: rid,
            sortKey: sortKey,
            sender: sender,
            isOut: isOut,
            date: date,
            fullDate: fullDate,
            state: state,
            content: content
        };
    }-*/;

    protected JsMessage() {

    }
}