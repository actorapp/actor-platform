/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsDate;

import im.actor.core.api.ApiTextExMarkdown;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.ContactContent;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.FileLocalSource;
import im.actor.core.entity.content.FileRemoteSource;
import im.actor.core.entity.content.LocationContent;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.VoiceContent;
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
                } else if (value.getContent() instanceof VoiceContent) {
                    VoiceContent voiceContent = (VoiceContent) value.getContent();
                    content = JsContentVoice.create(fileName, fileExtension, fileSize, fileUrl,
                            isUploading, voiceContent.getDuration())
                } else {
                    content = JsContentDocument.create(fileName, fileExtension, fileSize,
                            thumb, fileUrl, isUploading);
                }

            } else if (value.getContent() instanceof ContactContent) {
                ContactContent contactContent = (ContactContent) value.getContent();
                JsArrayString phones = JsArray.createArray().cast();
                JsArrayString emails = JsArray.createArray().cast();
                for (String s : contactContent.getEmails()) {
                    emails.push(s);
                }
                for (String s : contactContent.getPhones()) {
                    phones.push(s);
                }
                content = JsContentContact.create(contactContent.getName(),
                        contactContent.getPhoto64(), phones, emails);
            } else if (value.getContent() instanceof LocationContent) {
                LocationContent locationContent = (LocationContent) value.getContent();

                content = JsContentLocation.create(locationContent.getLongitude(), locationContent.getLatitude(),
                        locationContent.getStreet(), locationContent.getPlace());
            } else {
                content = JsContentUnsupported.create();
            }

            JsArray<JsReaction> reactions = JsArray.createArray().cast();

            for (Reaction r : value.getReactions()) {
                JsArrayInteger uids = (JsArrayInteger) JsArrayInteger.createArray();
                boolean isOwnSet = false;
                for (Integer i : r.getUids()) {
                    uids.push(i);
                    if (i == messenger.myUid()) {
                        isOwnSet = true;
                    }
                }
                reactions.push(JsReaction.create(r.getCode(), uids, isOwnSet));
            }

            return create(rid, sortKey, sender, isOut, date, fullDate, Enums.convert(value.getMessageState()), isOnServer, content,
                    reactions);
        }
    };

    public native static JsMessage create(String rid, String sortKey, JsPeerInfo sender, boolean isOut, String date, JsDate fullDate, String state, boolean isOnServer, JsContent content,
                                          JsArray<JsReaction> reactions)/*-{
        return {
            rid: rid,
            sortKey: sortKey,
            sender: sender,
            isOut: isOut,
            date: date,
            fullDate: fullDate,
            state: state,
            isOnServer: isOnServer,
            content: content,
            reactions: reactions
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