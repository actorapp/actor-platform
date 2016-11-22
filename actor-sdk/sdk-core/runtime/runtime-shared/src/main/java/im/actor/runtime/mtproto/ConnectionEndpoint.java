/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class ConnectionEndpoint extends BserObject {

    public static final int TYPE_TCP = 0;
    public static final int TYPE_TCP_TLS = 1;
    public static final int TYPE_WS = 2;
    public static final int TYPE_WS_TLS = 3;

    @NotNull
    @Property("readonly, nonatomic")
    private String host;
    @Nullable
    @Property("readonly, nonatomic")
    private String knownIp;
    @Property("readonly, nonatomic")
    private int port;
    @Property("readonly, nonatomic")
    private int type;

    public ConnectionEndpoint() {
    }

    @ObjectiveCName("initWithHost:withPort:withKnownIp:withType:")
    public ConnectionEndpoint(@NotNull String host, int port, @Nullable String knownIp, int type) {
        this.host = host;
        this.port = port;
        this.type = type;
        this.knownIp = knownIp;
    }

    public int getType() {
        return type;
    }

    @NotNull
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Nullable
    public String getKnownIp() {
        return knownIp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionEndpoint that = (ConnectionEndpoint) o;

        if (port != that.port) return false;
        if (type != that.type) return false;
        if (!host.equals(that.host)) return false;
        return !(knownIp != null ? !knownIp.equals(that.knownIp) : that.knownIp != null);

    }

    public static ConnectionEndpoint fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ConnectionEndpoint(), data);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        host = values.getString(1);
        knownIp = values.optString(2);
        port = values.getInt(3);
        type = values.getInt(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(1, host);
        if (knownIp != null) {
            writer.writeString(2, knownIp);
        }
        writer.writeInt(3, port);
        writer.writeInt(4, type);
    }
}
