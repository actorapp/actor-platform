/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.DataInput;

public abstract class ObsoleteAbsContent extends BserObject {
    public static ObsoleteAbsContent contentFromBytes(byte[] data) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(new DataInput(data, 0, data.length)));
        ObsoleteContentType type = typeFromValue(reader.getInt(1));
        switch (type) {
            case TEXT:
                // return TextContent.textFromBytes(data);
            case DOCUMENT:
                // return DocumentContent.docFromBytes(data);
            case DOCUMENT_PHOTO:
                // return PhotoContent.photoFromBytes(data);
            case DOCUMENT_VIDEO:
                // return VideoContent.videoFromBytes(data);
            case SERVICE:
                // return ServiceContent.serviceFromBytes(data);
            case SERVICE_REGISTERED:
                // return ServiceUserRegistered.fromBytes(data);
            case SERVICE_CREATED:
                // return ServiceGroupCreated.fromBytes(data);
            case SERVICE_TITLE:
                // return ServiceGroupTitleChanged.fromBytes(data);
            case SERVICE_AVATAR:
                // return ServiceGroupAvatarChanged.fromBytes(data);
            case SERVICE_ADDED:
                // return ServiceGroupUserAdded.fromBytes(data);
            case SERVICE_KICKED:
                // return ServiceGroupUserKicked.fromBytes(data);
            case SERVICE_LEAVE:
                // return ServiceGroupUserLeave.fromBytes(data);
            default:
                throw new IOException("Unknown type");
        }
    }

    protected static ObsoleteContentType typeFromValue(int val) {
        switch (val) {
            default:
            case 1:
                return ObsoleteContentType.TEXT;
            case 2:
                return ObsoleteContentType.DOCUMENT;
            case 3:
                return ObsoleteContentType.DOCUMENT_PHOTO;
            case 4:
                return ObsoleteContentType.DOCUMENT_VIDEO;
            case 5:
                return ObsoleteContentType.SERVICE;
            case 6:
                return ObsoleteContentType.SERVICE_CREATED;
            case 7:
                return ObsoleteContentType.SERVICE_AVATAR;
            case 8:
                return ObsoleteContentType.SERVICE_TITLE;
            case 9:
                return ObsoleteContentType.SERVICE_ADDED;
            case 10:
                return ObsoleteContentType.SERVICE_KICKED;
            case 11:
                return ObsoleteContentType.SERVICE_LEAVE;
            case 12:
                return ObsoleteContentType.SERVICE_REGISTERED;
        }
    }
}
