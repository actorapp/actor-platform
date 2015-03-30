package im.actor.model.network;

/**
 * Created by ex3ndr on 07.02.15.
 */
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
