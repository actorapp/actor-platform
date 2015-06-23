/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.parser;

public abstract class Update extends HeaderBserObject {
    private boolean isLastInDiff;

    public boolean isLastInDiff() {
        return isLastInDiff;
    }

    public void setIsLastInDiff(boolean isLastInDiff) {
        this.isLastInDiff = isLastInDiff;
    }
}
