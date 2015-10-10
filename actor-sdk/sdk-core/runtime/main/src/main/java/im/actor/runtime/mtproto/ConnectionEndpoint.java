/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;

public class ConnectionEndpoint {
    private String host;
    private int port;
    private Type type;

    @ObjectiveCName("initWithHost:withPort:withType:")
    public ConnectionEndpoint(String host, int port, Type type) {
        this.host = host;
        this.port = port;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public enum Type {
        TCP, TCP_TLS, WS, WS_TLS
    }
}
