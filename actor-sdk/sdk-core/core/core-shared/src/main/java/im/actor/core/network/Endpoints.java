/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network;

import java.util.ArrayList;

import im.actor.runtime.mtproto.ConnectionEndpoint;

public class Endpoints {

    private int roundRobin = 0;
    private ConnectionEndpoint[] endpoints;
    private TrustedKey[] trustedKeys;

    public Endpoints(ConnectionEndpoint[] endpoints, TrustedKey[] trustedKeys) {
        this.endpoints = endpoints;
        this.trustedKeys = trustedKeys;
    }

    public TrustedKey[] getTrustedKeys() {
        return trustedKeys;
    }

    public ConnectionEndpoint fetchEndpoint(boolean preferEncrypted) {

        // Trying to find secure endpoint
        if (preferEncrypted) {
            ArrayList<ConnectionEndpoint> secure = new ArrayList<>();
            for (ConnectionEndpoint e : endpoints) {
                if (e.getType() == ConnectionEndpoint.TYPE_TCP_TLS ||
                        e.getType() == ConnectionEndpoint.TYPE_WS_TLS) {
                    secure.add(e);
                }
            }
            if (secure.size() > 0) {
                roundRobin = (roundRobin + 1) % secure.size();
                return secure.get(roundRobin);
            }
        } else {
            ArrayList<ConnectionEndpoint> plainText = new ArrayList<>();
            for (ConnectionEndpoint e : endpoints) {
                if (e.getType() == ConnectionEndpoint.TYPE_TCP ||
                        e.getType() == ConnectionEndpoint.TYPE_WS) {
                    plainText.add(e);
                }
            }
            if (plainText.size() > 0) {
                roundRobin = (roundRobin + 1) % plainText.size();
                return plainText.get(roundRobin);
            }
        }

        roundRobin = (roundRobin + 1) % endpoints.length;
        return endpoints[roundRobin];
    }
}
