/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;

import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.ContactContent;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.content.LocationContent;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.ServiceCallEnded;
import im.actor.core.entity.content.ServiceCallMissed;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.ServiceGroupAvatarChanged;
import im.actor.core.entity.content.ServiceGroupAboutChanged;
import im.actor.core.entity.content.ServiceGroupTopicChanged;
import im.actor.core.entity.content.ServiceGroupCreated;
import im.actor.core.entity.content.ServiceGroupTitleChanged;
import im.actor.core.entity.content.ServiceGroupUserInvited;
import im.actor.core.entity.content.ServiceGroupUserJoined;
import im.actor.core.entity.content.ServiceGroupUserKicked;
import im.actor.core.entity.content.ServiceGroupUserLeave;
import im.actor.core.entity.content.ServiceUserRegistered;
import im.actor.core.entity.content.StickerContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.VideoContent;
import im.actor.core.entity.content.VoiceContent;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;


public class ContentDescription extends BserObject {

    public static ContentDescription fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ContentDescription(), data);
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
