/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mtproto;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * Created by ex3ndr on 06.02.15.
 */
public interface Connection {
    @ObjectiveCName("postWithData:withOffset:withLength:")
    void post(byte[] data, int offset, int len);

    @ObjectiveCName("isClosed")
    boolean isClosed();

    @ObjectiveCName("close")
    void close();

    @ObjectiveCName("checkConnection")
    void checkConnection();
}
