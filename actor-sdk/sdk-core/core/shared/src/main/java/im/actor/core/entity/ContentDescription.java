/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;

import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.ServiceGroupAvatarChanged;
import im.actor.core.entity.content.ServiceGroupCreated;
import im.actor.core.entity.content.ServiceGroupTitleChanged;
import im.actor.core.entity.content.ServiceGroupUserInvited;
import im.actor.core.entity.content.ServiceGroupUserJoined;
import im.actor.core.entity.content.ServiceGroupUserKicked;
import im.actor.core.entity.content.ServiceGroupUserLeave;
import im.actor.core.entity.content.ServiceUserRegistered;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.VideoContent;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;


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
                    ((ServiceGroupUserInvited) msg).getAddedUid(), false);
        } else if (msg instanceof ServiceGroupUserKicked) {
            return new ContentDescription(ContentType.SERVICE_KICK, "",
                    ((ServiceGroupUserKicked) msg).getKickedUid(), false);
        } else if (msg instanceof ServiceGroupUserLeave) {
            return new ContentDescription(ContentType.SERVICE_LEAVE, "",
                    0, true);
        } else if (msg instanceof ServiceGroupUserJoined) {
            return new ContentDescription(ContentType.SERVICE_JOINED, "",
                    0, false);
        } else if (msg instanceof ServiceContent) {
            return new ContentDescription(ContentType.SERVICE,
                    ((ServiceContent) msg).getCompatText(), 0, false);
        } else {
            return new ContentDescription(ContentType.UNKNOWN_CONTENT);
        }
    }

    @Property("readonly, nonatomic")
    private ContentType contentType;
    @Property("readonly, nonatomic")
    private String text;
    @Property("readonly, nonatomic")
    private int relatedUser;
    @Property("readonly, nonatomic")
    private boolean isSilent;

    public ContentDescription(ContentType contentType, String text, int relatedUser,
                              boolean isSilent) {
        this.contentType = contentType;
        this.text = text;
        this.relatedUser = relatedUser;
        this.isSilent = isSilent;
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
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, contentType.getValue());
        writer.writeString(2, text);
        writer.writeInt(3, relatedUser);
        writer.writeBool(4, isSilent);
    }
}
