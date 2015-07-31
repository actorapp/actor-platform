package im.actor.model.tcp;/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import im.actor.model.network.connection.ManagedNetworkProvider;

public class TcpNetworkProvider extends ManagedNetworkProvider {

    public TcpNetworkProvider() {
        super(new AsyncTcpConnectionFactory());
    }
}