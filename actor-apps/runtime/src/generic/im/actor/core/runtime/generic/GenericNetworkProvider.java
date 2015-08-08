package im.actor.core.runtime.generic;/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import im.actor.core.runtime.generic.network.AsyncTcpConnectionFactory;
import im.actor.runtime.mtproto.ManagedNetworkProvider;

public class GenericNetworkProvider extends ManagedNetworkProvider {

    public GenericNetworkProvider() {
        super(new AsyncTcpConnectionFactory());
    }
}