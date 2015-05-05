/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network;

/**
 * Created by ex3ndr on 06.02.15.
 */
public interface Connection {
    public void post(byte[] data, int offset, int len);

    public boolean isClosed();

    public void close();
}
