/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

public class ContentLocalContainer extends AbsContentContainer {
    private AbsLocalContent content;

    public ContentLocalContainer(AbsLocalContent content) {
        this.content = content;
    }

    public AbsLocalContent getContent() {
        return content;
    }
}
