package im.actor.model.network.parser;

import im.actor.model.droidkit.bser.BserObject;

/**
 * Created by ex3ndr on 15.11.14.
 */
public abstract class HeaderBserObject extends BserObject {
    public abstract int getHeaderKey();
}
