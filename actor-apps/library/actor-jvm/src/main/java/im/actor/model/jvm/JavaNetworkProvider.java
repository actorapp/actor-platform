/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.jvm;

import im.actor.model.jvm.tcp.AsyncTcpConnectionFactory;
import im.actor.model.network.connection.ManagedNetworkProvider;

public class JavaNetworkProvider extends ManagedNetworkProvider {

    public JavaNetworkProvider() {
        super(new AsyncTcpConnectionFactory());
    }
}