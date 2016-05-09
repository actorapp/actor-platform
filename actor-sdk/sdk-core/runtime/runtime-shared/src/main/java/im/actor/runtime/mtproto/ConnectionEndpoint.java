/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectionEndpoint {

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
}
