/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages.entity;

import im.actor.model.entity.Group;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.content.FastThumb;

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

//    public static Avatar convert(im.actor.model.api.Avatar avatar) {
//        if (avatar == null) {
//            return null;
//        }
//        return new Avatar(avatar);
//    }

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


    public static FastThumb convert(im.actor.model.api.FastThumb fastThumb) {
        if (fastThumb == null) {
            return null;
        }
        return new FastThumb(fastThumb.getW(), fastThumb.getH(), fastThumb.getThumb());
    }
}
