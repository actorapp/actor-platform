/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectionEndpoint {

    @NotNull
    @Property("readonly, nonatomic")
    private String host;
    @Nullable
    @Property("readonly, nonatomic")
    private String knownIp;
    @Property("readonly, nonatomic")
    private int port;
    @NotNull
    @Property("readonly, nonatomic")
    private Type type;

    @ObjectiveCName("initWithHost:withPort:withKnownIp:withType:")
    public ConnectionEndpoint(@NotNull String host, int port, @Nullable String knownIp, @NotNull Type type) {
        this.host = host;
        this.port = port;
        this.type = type;
        this.knownIp = knownIp;
    }

    @NotNull
    public Type getType() {
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

    public enum Type {
        TCP, TCP_TLS, WS, WS_TLS
    }
}
