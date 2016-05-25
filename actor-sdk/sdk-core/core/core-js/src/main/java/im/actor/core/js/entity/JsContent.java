/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

import im.actor.core.api.ApiTextModernAttach;
import im.actor.core.api.ApiTextModernField;
import im.actor.core.api.ApiTextModernMessage;
import im.actor.core.entity.ImageLocation;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.AnimationContent;
import im.actor.core.entity.content.ContactContent;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.FileLocalSource;
import im.actor.core.entity.content.FileRemoteSource;
import im.actor.core.entity.content.LocationContent;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.StickerContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.VoiceContent;
import im.actor.core.js.JsMessenger;
import im.actor.runtime.crypto.Base64Utils;

public abstract class JsContent extends JavaScriptObject {

    public static JsContent createContent(AbsContent src, int sender) {
        JsMessenger messenger = JsMessenger.getInstance();
        JsContent content;
        if (src instanceof TextContent) {
            TextContent textContent = (TextContent) src;
            if (textContent.getTextMessageEx() instanceof ApiTextModernMessage) {
                ApiTextModernMessage modernMessage = (ApiTextModernMessage) textContent.getTextMessageEx();
                String text = modernMessage.getText();
                JsParagraphStyle paragraphStyle = JsParagraphStyle.create(modernMessage.getStyle());
                JsArray<JsAttach> attaches = JsArray.createArray().cast();
                for (ApiTextModernAttach srcAttach : modernMessage.getAttaches()) {
                    JsArray<JsAttachField> fields = JsArray.createArray().cast();
                    for (ApiTextModernField f : srcAttach.getFields()) {
                        boolean isShort = f.isShort() != null ? f.isShort() : true;
                        fields.push(JsAttachField.create(f.getTitle(), f.getValue(),
                                isShort));
                    }
                    attaches.push(JsAttach.create(
                            srcAttach.getTitle(),
                            srcAttach.getTitleUrl(),
                            srcAttach.getText(),
                            JsParagraphStyle.create(srcAttach.getStyle()),
                            fields));
                }
                content = JsContentTextModern.create(text, paragraphStyle, attaches);
            } else {
                content = JsContentText.create(((TextContent) src).getText());
            }
        } else if (src instanceof ServiceContent) {
            content = JsContentService.create(messenger.getFormatter().formatFullServiceMessage(sender, (ServiceContent) src));
        } else if (src instanceof DocumentContent) {
            DocumentContent doc = (DocumentContent) src;

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

            if (src instanceof PhotoContent && thumb != null) {
                PhotoContent photoContent = (PhotoContent) src;
                content = JsContentPhoto.create(
                        fileName, fileExtension, fileSize,
                        photoContent.getW(), photoContent.getH(), thumb,
                        fileUrl, isUploading);
            } else if (src instanceof AnimationContent) {
                AnimationContent animationContent = (AnimationContent) src;
                content = JsContentAnimation.create(fileName, fileExtension, fileSize,
                        animationContent.getW(), animationContent.getH(), thumb,
                        fileUrl, isUploading);
            } else if (src instanceof VoiceContent) {
                VoiceContent voiceContent = (VoiceContent) src;
                content = JsContentVoice.create(fileName, fileExtension, fileSize, fileUrl,
                        isUploading, voiceContent.getDuration());
            } else {
                content = JsContentDocument.create(fileName, fileExtension, fileSize,
                        thumb, fileUrl, isUploading);
            }

        } else if (src instanceof StickerContent) {
            StickerContent sticker = (StickerContent) src;
            ImageLocation stickerImage = sticker.getImage256();
            if (sticker.getImage512() != null) {
                stickerImage = sticker.getImage512();
            }
            String fileUrl = messenger.getFileUrl(stickerImage.getReference());
            String fileSize = messenger.getFormatter().formatFileSize(stickerImage.getReference().getFileSize());
            content = JsContentSticker.create(
                    stickerImage.getReference().getFileName(),
                    ".webp", fileSize,
                    stickerImage.getWidth(), stickerImage.getHeight(), null, fileUrl, false);
        } else if (src instanceof ContactContent) {
            ContactContent contactContent = (ContactContent) src;
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
        } else if (src instanceof LocationContent) {
            LocationContent locationContent = (LocationContent) src;

            content = JsContentLocation.create(locationContent.getLongitude(), locationContent.getLatitude(),
                    locationContent.getStreet(), locationContent.getPlace());
        } else {
            content = JsContentUnsupported.create();
        }
        return content;
    }

    protected JsContent() {

    }
}
