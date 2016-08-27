package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ConnectionEndpointArray extends ArrayList<ConnectionEndpoint> {
    @NotNull
    @ObjectiveCName("addEndpoint:")
    public ConnectionEndpointArray addEndpoint(@NotNull String url) throws UnknownSchemeException {

        // Manual buggy parsing for GWT
        // TODO: Correct URL parsing
        String scheme = url.substring(0, url.indexOf(":")).toLowerCase();
        String host = url.substring(url.indexOf("://") + "://".length());
        String knownIp = null;

        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        int port = -1;
        if (host.contains(":")) {
            String[] parts = host.split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        }

        if (host.contains("@")) {
            String[] parts = host.split("@");
            host = parts[0];
            knownIp = parts[1];
        }

        if (scheme.equals("ssl") || scheme.equals("tls")) {
            if (port <= 0) {
                port = 443;
            }
            add(new ConnectionEndpoint(host, port, knownIp, ConnectionEndpoint.TYPE_TCP_TLS));
        } else if (scheme.equals("tcp")) {
            if (port <= 0) {
                port = 80;
            }
            add(new ConnectionEndpoint(host, port, knownIp, ConnectionEndpoint.TYPE_TCP));
        } else if (scheme.equals("ws")) {
            if (port <= 0) {
                port = 80;
            }
            add(new ConnectionEndpoint(host, port, knownIp, ConnectionEndpoint.TYPE_WS));
        } else if (scheme.equals("wss")) {
            if (port <= 0) {
                port = 443;
            }
            add(new ConnectionEndpoint(host, port, knownIp, ConnectionEndpoint.TYPE_WS_TLS));
        } else {
            throw new UnknownSchemeException("Unknown scheme type: " + scheme);
        }
        return this;
    }

    public class UnknownSchemeException extends Exception {
        public UnknownSchemeException(String message) {
            super(message);
        }
    }
}