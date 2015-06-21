package im.actor.android;/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import im.actor.model.network.connection.ManagedNetworkProvider;

public class AndroidNetworkProvider extends ManagedNetworkProvider {

    public AndroidNetworkProvider() {
        super(new im.actor.android.tcp.AsyncTcpConnectionFactory());
    }
}