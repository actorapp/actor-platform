/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.parser;

import java.io.IOException;

public abstract class BaseParser<T> {
    public abstract T read(int type, byte[] payload) throws IOException;
}
