/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages.entity;

import im.actor.core.api.ApiFastThumb;
import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiMessageState;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.entity.GroupEntity;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.PeerEntity;
import im.actor.core.entity.PeerTypeEntity;
import im.actor.core.entity.content.FastThumb;

public class EntityConverter {

    public static MessageState convert(ApiMessageState state) {
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

    public static GroupEntity convert(ApiGroup group) {
        return new GroupEntity(group);
    }

    public static PeerTypeEntity convert(ApiPeerType peerType) {
        switch (peerType) {
            case GROUP:
                return PeerTypeEntity.GROUP;
            default:
            case PRIVATE:
                return PeerTypeEntity.PRIVATE;
        }
    }

    public static PeerEntity convert(ApiPeer peer) {
        return new PeerEntity(convert(peer.getType()), peer.getId());
    }


    public static FastThumb convert(ApiFastThumb fastThumb) {
        if (fastThumb == null) {
            return null;
        }
        return new FastThumb(fastThumb.getW(), fastThumb.getH(), fastThumb.getThumb());
    }
}
