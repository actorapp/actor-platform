/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.mtproto.ConnectionEndpoint;

public class Endpoints extends BserObject {

    private int roundRobin = 0;
    private ConnectionEndpoint[] endpoints;
    private TrustedKey[] trustedKeys;

    public Endpoints() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Endpoints endpoints1 = (Endpoints) o;

        return Arrays.equals(endpoints, endpoints1.endpoints) && Arrays.equals(trustedKeys, endpoints1.trustedKeys);
    }

    public static Endpoints fromBytes(byte[] data) throws IOException {
        return Bser.parse(new Endpoints(), data);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        List<byte[]> endpointsRepeatedBytes = values.getRepeatedBytes(1);
        endpoints = new ConnectionEndpoint[endpointsRepeatedBytes.size()];
        for (int i = 0; i < endpoints.length; i++) {
            endpoints[i] = ConnectionEndpoint.fromBytes(endpointsRepeatedBytes.get(i));
        }

        List<byte[]> trustedKeysRepeatedBytes = values.getRepeatedBytes(2);
        trustedKeys = new TrustedKey[trustedKeysRepeatedBytes.size()];
        for (int i = 0; i < trustedKeys.length; i++) {
            trustedKeys[i] = TrustedKey.fromBytes(trustedKeysRepeatedBytes.get(i));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, new ArrayList<>(Arrays.asList(endpoints)));
        writer.writeRepeatedObj(2, new ArrayList<>(Arrays.asList(trustedKeys)));
    }
}
