package im.actor.model.network.parser;

import java.io.IOException;

/**
 * Created by ex3ndr on 15.11.14.
 */
public abstract class BaseParser<T> {
    public abstract T read(int type, byte[] payload) throws IOException;
}
