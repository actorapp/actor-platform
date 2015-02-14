package im.actor.model.modules.messages;

import im.actor.model.entity.Dialog;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.ServiceGroupAvatarChanged;
import im.actor.model.entity.content.ServiceGroupCreated;
import im.actor.model.entity.content.ServiceGroupTitleChanged;
import im.actor.model.entity.content.ServiceGroupUserAdded;
import im.actor.model.entity.content.ServiceGroupUserKicked;
import im.actor.model.entity.content.ServiceGroupUserLeave;
import im.actor.model.entity.content.ServiceUserRegistered;
import im.actor.model.entity.content.TextContent;
import im.actor.model.entity.content.VideoContent;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class ContentDescription {

    private Dialog.ContentType contentType;
    private String text;
    private int relatedUid;
    private boolean isSilent;

    public ContentDescription(AbsContent msg) {
        if (msg instanceof TextContent) {
            contentType = Dialog.ContentType.TEXT;
            text = ((TextContent) msg).getText();
        } else if (msg instanceof PhotoContent) {
            contentType = Dialog.ContentType.DOCUMENT_PHOTO;
            text = "";
        } else if (msg instanceof VideoContent) {
            contentType = Dialog.ContentType.DOCUMENT_VIDEO;
            text = "";
        } else if (msg instanceof DocumentContent) {
            contentType = Dialog.ContentType.DOCUMENT;
            text = "";
        } else if (msg instanceof ServiceUserRegistered) {
            contentType = Dialog.ContentType.SERVICE_REGISTERED;
            text = "";
        } else if (msg instanceof ServiceGroupAvatarChanged) {
            if (((ServiceGroupAvatarChanged) msg).getNewAvatar() == null) {
                contentType = Dialog.ContentType.SERVICE_AVATAR_REMOVED;
            } else {
                contentType = Dialog.ContentType.SERVICE_AVATAR;
            }
            text = "";
        } else if (msg instanceof ServiceGroupTitleChanged) {
            contentType = Dialog.ContentType.SERVICE_TITLE;
            text = "";
        } else if (msg instanceof ServiceGroupCreated) {
            contentType = Dialog.ContentType.SERVICE_CREATED;
            text = "";
        } else if (msg instanceof ServiceGroupUserAdded) {
            contentType = Dialog.ContentType.SERVICE_ADD;
            text = "";
            relatedUid = ((ServiceGroupUserAdded) msg).getAddedUid();
        } else if (msg instanceof ServiceGroupUserKicked) {
            contentType = Dialog.ContentType.SERVICE_KICK;
            text = "";
            relatedUid = ((ServiceGroupUserKicked) msg).getKickedUid();
        } else if (msg instanceof ServiceGroupUserLeave) {
            contentType = Dialog.ContentType.SERVICE_LEAVE;
            text = "";
            isSilent = true;
        } else {
            throw new RuntimeException("Unknown content type");
        }
    }

    public boolean isSilent() {
        return isSilent;
    }

    public Dialog.ContentType getContentType() {
        return contentType;
    }

    public String getText() {
        return text;
    }

    public int getRelatedUid() {
        return relatedUid;
    }
}
