/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

public class Endpoints {
    private int roundRobin = 0;
    private ConnectionEndpoint[] endpoints;

    public Endpoints(ConnectionEndpoint[] endpoints) {
        this.endpoints = endpoints;
    }

    public ConnectionEndpoint fetchEndpoint() {
        roundRobin = (roundRobin + 1) % endpoints.length;
        return endpoints[roundRobin];
    }
}
