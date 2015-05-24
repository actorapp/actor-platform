/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.Message;
import im.actor.model.api.ServiceExContactRegistered;
import im.actor.model.api.ServiceExGroupCreated;
import im.actor.model.api.ServiceExUserLeft;
import im.actor.model.api.ServiceMessage;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.entity.content.AbsContentContainer;
import im.actor.model.entity.content.ContentServerContainer;

public abstract class ObsoleteAbsContent extends BserObject {
    public static AbsContentContainer contentFromBytes(byte[] data) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(new DataInput(data, 0, data.length)));
        ObsoleteContentType type = typeFromValue(reader.getInt(1));
        switch (type) {
            case TEXT:
                return new ContentServerContainer(new ObsoleteTextContent(data).toApiMessage());
            case DOCUMENT:
                // return DocumentContent.docFromBytes(data);
            case DOCUMENT_PHOTO:
                // return PhotoContent.photoFromBytes(data);
            case DOCUMENT_VIDEO:
                // return VideoContent.videoFromBytes(data);
            case SERVICE:
                return new ContentServerContainer(new ObsoleteService(data).toApiMessage());
            case SERVICE_REGISTERED:
                return new ContentServerContainer(new ServiceMessage("User registered",
                        new ServiceExContactRegistered(0/*Old service message doesn't contain uid*/)));
            case SERVICE_CREATED:
                return new ContentServerContainer(new ServiceMessage("Group created", new ServiceExGroupCreated()));
            case SERVICE_TITLE:
                return new ContentServerContainer(new ObsoleteServiceTitle(data).toApiMessage());
            case SERVICE_AVATAR:
                return new ContentServerContainer(new ObsoleteServiceAvatar(data).toApiMessage());
            case SERVICE_ADDED:
                return new ContentServerContainer(new ObsoleteServiceAdded(data).toApiMessage());
            case SERVICE_KICKED:
                return new ContentServerContainer(new ObsoleteServiceKicked(data).toApiMessage());
            case SERVICE_LEAVE:
                return new ContentServerContainer(new ServiceMessage("User leave", new ServiceExUserLeft()));
            default:
                throw new IOException("Unknown type");
        }
    }

    public abstract Message toApiMessage();

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
