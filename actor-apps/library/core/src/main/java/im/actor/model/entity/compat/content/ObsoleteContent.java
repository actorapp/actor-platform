/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

import java.io.IOException;

import im.actor.model.api.ServiceExContactRegistered;
import im.actor.model.api.ServiceExGroupCreated;
import im.actor.model.api.ServiceExUserLeft;
import im.actor.model.api.ServiceMessage;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.entity.content.internal.AbsContentContainer;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class ObsoleteContent {

    private static final int TEXT = 1;
    private static final int DOCUMENT = 2;
    private static final int DOCUMENT_PHOTO = 3;
    private static final int DOCUMENT_VIDEO = 4;
    private static final int SERVICE = 5;
    private static final int SERVICE_CREATED = 6;
    private static final int SERVICE_AVATAR = 7;
    private static final int SERVICE_TITLE = 8;
    private static final int SERVICE_ADDED = 9;
    private static final int SERVICE_KICKED = 10;
    private static final int SERVICE_LEAVE = 11;
    private static final int SERVICE_REGISTERED = 12;

    public static AbsContentContainer contentFromValues(BserValues values) throws IOException {
        switch (values.getInt(1)) {
            case TEXT:
                return new ContentRemoteContainer(new ObsoleteText(values).toApiMessage());
            case DOCUMENT:
                return new ObsoleteDocument(values).toContainer();
            case DOCUMENT_PHOTO:
                return new ObsoletePhoto(values).toContainer();
            case DOCUMENT_VIDEO:
                return new ObsoleteVideo(values).toContainer();
            case SERVICE:
                return new ContentRemoteContainer(new ObsoleteService(values).toApiMessage());
            case SERVICE_REGISTERED:
                return new ContentRemoteContainer(new ServiceMessage("User registered",
                        new ServiceExContactRegistered(0/*Old service message doesn't contain uid*/)));
            case SERVICE_CREATED:
                return new ContentRemoteContainer(new ServiceMessage("Group created", new ServiceExGroupCreated()));
            case SERVICE_TITLE:
                return new ContentRemoteContainer(new ObsoleteServiceTitle(values).toApiMessage());
            case SERVICE_AVATAR:
                return new ContentRemoteContainer(new ObsoleteServiceAvatar(values).toApiMessage());
            case SERVICE_ADDED:
                return new ContentRemoteContainer(new ObsoleteServiceAdded(values).toApiMessage());
            case SERVICE_KICKED:
                return new ContentRemoteContainer(new ObsoleteServiceKicked(values).toApiMessage());
            case SERVICE_LEAVE:
                return new ContentRemoteContainer(new ServiceMessage("User leave", new ServiceExUserLeft()));
            default:
                throw new IOException("Unknown type");
        }
    }
}
