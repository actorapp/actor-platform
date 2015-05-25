/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.util.ArrayList;

import im.actor.model.api.TextMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class TextContent extends AbsContent {

    public static TextContent create(String text) {
        return new TextContent(new ContentRemoteContainer(new TextMessage(text,
                new ArrayList<Integer>(), null)));
    }

    private String text;

    public TextContent(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);
        text = ((TextMessage) remoteContainer.getMessage()).getText();
    }

    public String getText() {
        return text;
    }
}
