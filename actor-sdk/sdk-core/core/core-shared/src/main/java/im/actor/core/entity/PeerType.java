/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import im.actor.core.api.ApiPeerType;

public enum PeerType {
    PRIVATE, GROUP, PRIVATE_ENCRYPTED;

    public ApiPeerType toApi() {
        switch (this) {
            case GROUP:
                return ApiPeerType.GROUP;
            case PRIVATE:
                return ApiPeerType.PRIVATE;
            case PRIVATE_ENCRYPTED:
                return ApiPeerType.ENCRYPTEDPRIVATE;
            default:
                return ApiPeerType.UNSUPPORTED_VALUE;
        }

    }
}
