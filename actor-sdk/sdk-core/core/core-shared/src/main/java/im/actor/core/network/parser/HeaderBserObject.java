/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.parser;

import im.actor.runtime.bser.BserObject;

public abstract class HeaderBserObject extends BserObject {
    public abstract int getHeaderKey();
}
