package im.actor.model.jvm;

import im.actor.model.jvm.tcp.AsyncTcpConnectionFactory;
import im.actor.model.network.connection.ManagedNetworkProvider;

/**
 * Created by ex3ndr on 13.04.15.
 */
public class JavaNetworkProvider extends ManagedNetworkProvider {

    public JavaNetworkProvider() {
        super(new AsyncTcpConnectionFactory());
    }
}