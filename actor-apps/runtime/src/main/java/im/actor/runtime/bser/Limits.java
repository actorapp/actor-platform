/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

final class Limits {
    public static final int MAX_BLOCK_SIZE = 1024 * 1024;// 1 MB
    public static final int MAX_PROTO_REPEATED = 1024 * 1024;

    private Limits() {
    }
}
