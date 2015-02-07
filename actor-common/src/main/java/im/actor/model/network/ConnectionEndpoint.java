package im.actor.model.network;

/**
 * Created by ex3ndr on 06.02.15.
 */
public class ConnectionEndpoint {
    private String host;
    private int port;
    private Type type;

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
