/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;

import im.actor.core.api.ApiTextExMarkdown;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.FileLocalSource;
import im.actor.core.entity.content.FileRemoteSource;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.js.JsMessenger;
import im.actor.runtime.crypto.Base64Utils;
import im.actor.runtime.js.mvvm.JsEntityConverter;

public class JsMessage extends JavaScriptObject {

    public static final JsEntityConverter<Message, JsMessage> CONVERTER = new JsEntityConverter<Message, JsMessage>() {
        @Override
        public JsMessage convert(Message value) {
            JsMessenger messenger = JsMessenger.getInstance();

            String rid = value.getRid() + "";
            String sortKey = value.getSortDate() + "";

            JsPeerInfo sender = messenger.buildPeerInfo(Peer.user(value.getSenderId()));
            boolean isOut = value.getSenderId() == messenger.myUid();
            boolean isOnServer = value.isOnServer();
            String date = messenger.getFormatter().formatTime(value.getDate());
            JsDate fullDate = JsDate.create(value.getDate());

            JsContent content;
            if (value.getContent() instanceof TextContent) {
                TextContent textContent = (TextContent) value.getContent();

                String text = ((TextContent) value.getContent()).getText();

                String markdownText = null;
                if (textContent.getTextMessageEx() instanceof ApiTextExMarkdown) {
                    markdownText = ((ApiTextExMarkdown) textContent.getTextMessageEx()).getMarkdown();
                }

                content = JsContentText.create(text, markdownText);
            } else if (value.getContent() instanceof ServiceContent) {
                content = JsContentService.create(messenger.getFormatter().formatFullServiceMessage(value.getSenderId(), (ServiceContent) value.getContent()));
            } else if (value.getContent() instanceof DocumentContent) {
                DocumentContent doc = (DocumentContent) value.getContent();

                String fileName = doc.getName();
                String fileExtension = doc.getExt();
                String fileSize = messenger.getFormatter().formatFileSize(doc.getSource().getSize());
                String fileUrl = null;

                if (doc.getSource() instanceof FileRemoteSource) {
                    fileUrl = messenger.getFileUrl(((FileRemoteSource) doc.getSource()).getFileReference());
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

            return create(rid, sortKey, sender, isOut, date, fullDate, Enums.convert(value.getMessageState()), isOnServer, content);
        }
    };

    public native static JsMessage create(String rid, String sortKey, JsPeerInfo sender, boolean isOut, String date, JsDate fullDate, String state, boolean isOnServer, JsContent content)/*-{
        return {
            rid: rid,
            sortKey: sortKey,
            sender: sender,
            isOut: isOut,
            date: date,
            fullDate: fullDate,
            state: state,
            isOnServer: isOnServer,
            content: content
        };
    }-*/;

    protected JsMessage() {

    }

    public native final String getSortKey()/*-{
        return this.sortKey;
    }-*/;

    public native final boolean isOnServer()/*-{
        return this.isOnServer;
    }-*/;

}