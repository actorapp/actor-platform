/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages.entity;

import im.actor.model.api.DocumentExPhoto;
import im.actor.model.api.DocumentExVideo;
import im.actor.model.api.DocumentMessage;
import im.actor.model.api.FileLocation;
import im.actor.model.api.ServiceEx;
import im.actor.model.api.ServiceExChangedAvatar;
import im.actor.model.api.ServiceExChangedTitle;
import im.actor.model.api.ServiceExGroupCreated;
import im.actor.model.api.ServiceExUserAdded;
import im.actor.model.api.ServiceExUserKicked;
import im.actor.model.api.ServiceExUserLeft;
import im.actor.model.api.ServiceMessage;
import im.actor.model.api.TextMessage;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Group;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FastThumb;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.ServiceContent;
import im.actor.model.entity.content.ServiceGroupAvatarChanged;
import im.actor.model.entity.content.ServiceGroupCreated;
import im.actor.model.entity.content.ServiceGroupTitleChanged;
import im.actor.model.entity.content.ServiceGroupUserAdded;
import im.actor.model.entity.content.ServiceGroupUserKicked;
import im.actor.model.entity.content.ServiceGroupUserLeave;
import im.actor.model.entity.content.TextContent;
import im.actor.model.entity.content.VideoContent;

public class EntityConverter {

    public static MessageState convert(im.actor.model.api.MessageState state) {
        if (state == null) {
            return MessageState.UNKNOWN;
        }
        switch (state) {
            case READ:
                return MessageState.READ;
            case RECEIVED:
                return MessageState.RECEIVED;
            case SENT:
                return MessageState.SENT;
            default:
                return MessageState.UNKNOWN;
        }
    }

    public static Avatar convert(im.actor.model.api.Avatar avatar) {
        if (avatar == null) {
            return null;
        }
        return new Avatar(avatar);
    }

    public static Group convert(im.actor.model.api.Group group) {
        return new Group(group);
    }

    public static PeerType convert(im.actor.model.api.PeerType peerType) {
        switch (peerType) {
            case GROUP:
                return PeerType.GROUP;
            default:
            case PRIVATE:
                return PeerType.PRIVATE;
        }
    }

    public static Peer convert(im.actor.model.api.Peer peer) {
        return new Peer(convert(peer.getType()), peer.getId());
    }

    public static AbsContent convert(im.actor.model.api.Message content) {
        if (content instanceof TextMessage) {
            TextMessage message = (TextMessage) content;
            return new TextContent(message.getText());
        } else if (content instanceof ServiceMessage) {
            ServiceMessage message = (ServiceMessage) content;
            ServiceEx ex = message.getExt();
            if (ex instanceof ServiceExChangedAvatar) {
                ServiceExChangedAvatar avatar = (ServiceExChangedAvatar) ex;
                return new ServiceGroupAvatarChanged(convert(avatar.getAvatar()));
            } else if (ex instanceof ServiceExChangedTitle) {
                ServiceExChangedTitle title = (ServiceExChangedTitle) ex;
                return new ServiceGroupTitleChanged(title.getTitle());
            } else if (ex instanceof ServiceExUserAdded) {
                ServiceExUserAdded userAdded = (ServiceExUserAdded) ex;
                return new ServiceGroupUserAdded(userAdded.getAddedUid());
            } else if (ex instanceof ServiceExUserKicked) {
                ServiceExUserKicked exUserKicked = (ServiceExUserKicked) ex;
                return new ServiceGroupUserKicked(exUserKicked.getKickedUid());
            } else if (ex instanceof ServiceExUserLeft) {
                return new ServiceGroupUserLeave();
            } else if (ex instanceof ServiceExGroupCreated) {
                // TODO: Fix
                return new ServiceGroupCreated("");
            } else {
                return new ServiceContent(message.getText());
            }
        } else if (content instanceof DocumentMessage) {
            DocumentMessage documentMessage = (DocumentMessage) content;

            String mimeType = documentMessage.getMimeType();
            String name = documentMessage.getName();
            im.actor.model.entity.content.FastThumb fastThumb = convert(documentMessage.getThumb());
            FileReference fileReference = new FileReference(
                    new FileLocation(documentMessage.getFileId(),
                            documentMessage.getAccessHash()),
                    documentMessage.getName(),
                    documentMessage.getFileSize());
            FileRemoteSource source = new FileRemoteSource(fileReference);

            if (documentMessage.getExt() instanceof DocumentExPhoto) {
                DocumentExPhoto photo = (DocumentExPhoto) documentMessage.getExt();
                return new PhotoContent(source, mimeType, name, fastThumb, photo.getW(), photo.getH());
            } else if (documentMessage.getExt() instanceof DocumentExVideo) {
                DocumentExVideo video = (DocumentExVideo) documentMessage.getExt();
                return new VideoContent(source, mimeType, name, fastThumb,
                        video.getDuration(), video.getW(), video.getH());
            } else {
                return new DocumentContent(source, mimeType, name, fastThumb);
            }
        }
        return null;
    }

    public static FastThumb convert(im.actor.model.api.FastThumb fastThumb) {
        if (fastThumb == null) {
            return null;
        }
        return new FastThumb(fastThumb.getW(), fastThumb.getH(), fastThumb.getThumb());
    }
}
