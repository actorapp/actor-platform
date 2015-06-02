/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.ServiceGroupAvatarChanged;
import im.actor.model.entity.content.ServiceGroupCreated;
import im.actor.model.entity.content.ServiceGroupTitleChanged;

import im.actor.model.entity.content.ServiceGroupUserInvited;
import im.actor.model.entity.content.ServiceGroupUserJoined;
import im.actor.model.entity.content.ServiceGroupUserKicked;
import im.actor.model.entity.content.ServiceGroupUserLeave;
import im.actor.model.entity.content.ServiceUserRegistered;
import im.actor.model.entity.content.TextContent;
import im.actor.model.entity.content.VideoContent;


public class ContentDescription extends BserObject {

    public static ContentDescription fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ContentDescription(), data);
    }

    public static ContentDescription fromContent(AbsContent msg) {
        if (msg instanceof TextContent) {
            return new ContentDescription(ContentType.TEXT,
                    ((TextContent) msg).getText());
        } else if (msg instanceof PhotoContent) {
            return new ContentDescription(ContentType.DOCUMENT_PHOTO);
        } else if (msg instanceof VideoContent) {
            return new ContentDescription(ContentType.DOCUMENT_VIDEO);
        } else if (msg instanceof DocumentContent) {
            return new ContentDescription(ContentType.DOCUMENT);
        } else if (msg instanceof ServiceUserRegistered) {
            return new ContentDescription(ContentType.SERVICE_REGISTERED);
        } else if (msg instanceof ServiceGroupAvatarChanged) {
            if (((ServiceGroupAvatarChanged) msg).getNewAvatar() == null) {
                return new ContentDescription(ContentType.SERVICE_AVATAR_REMOVED);
            } else {
                return new ContentDescription(ContentType.SERVICE_AVATAR);
            }
        } else if (msg instanceof ServiceGroupTitleChanged) {
            return new ContentDescription(ContentType.SERVICE_TITLE,
                    ((ServiceGroupTitleChanged) msg).getNewTitle());
        } else if (msg instanceof ServiceGroupCreated) {
            return new ContentDescription(ContentType.SERVICE_CREATED);
        } else if (msg instanceof ServiceGroupUserInvited) {
            return new ContentDescription(ContentType.SERVICE_ADD, "",
                    ((ServiceGroupUserInvited) msg).getInvitedUid(), false);
        } else if (msg instanceof ServiceGroupUserKicked) {
            return new ContentDescription(ContentType.SERVICE_KICK, "",
                    ((ServiceGroupUserKicked) msg).getKickedUid(), false);
        } else if (msg instanceof ServiceGroupUserLeave) {
            return new ContentDescription(ContentType.SERVICE_LEAVE, "",
                    0, true);
        } else if (msg instanceof ServiceGroupUserJoined) {
            return new ContentDescription(ContentType.SERVICE_JOINED, "",
                    0, true);
        } else {
            return new ContentDescription(ContentType.UNKNOWN_CONTENT);
        }
    }

    private ContentType contentType;
    private String text;
    private int relatedUser;
    private boolean isSilent;
    private boolean isEncrypted;

    public ContentDescription(ContentType contentType, String text, int relatedUser,
                              boolean isSilent) {
        this.contentType = contentType;
        this.text = text;
        this.relatedUser = relatedUser;
        this.isSilent = isSilent;
        this.isEncrypted = false;
    }

    public ContentDescription(ContentType contentType, String text) {
        this(contentType, text, 0, false);
    }

    public ContentDescription(ContentType contentType) {
        this(contentType, "", 0, false);
    }

    private ContentDescription() {

    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getText() {
        return text;
    }

    public int getRelatedUser() {
        return relatedUser;
    }

    public boolean isSilent() {
        return isSilent;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        contentType = ContentType.fromValue(values.getInt(1));
        text = values.getString(2);
        relatedUser = values.getInt(3);
        isSilent = values.getBool(4);
        isEncrypted = values.getBool(5);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, contentType.getValue());
        writer.writeString(2, text);
        writer.writeInt(3, relatedUser);
        writer.writeBool(4, isSilent);
        writer.writeBool(5, isEncrypted);
    }
}
