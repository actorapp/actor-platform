/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.util.ArrayList;

import im.actor.model.api.TextMessage;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class TextContent extends AbsContent {

    public static TextContent create(String text, ArrayList<Integer> mentions) {
        return new TextContent(new ContentRemoteContainer(new TextMessage(text,
                mentions, null)));
    }

    private String text;
    private ArrayList<Integer> mentions;

    public TextContent(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);
        text = ((TextMessage) remoteContainer.getMessage()).getText();
        mentions = (ArrayList<Integer>) ((TextMessage) remoteContainer.getMessage()).getMentions();
    }

    public ArrayList<Integer> getMentions() {
        return mentions;
    }

    public String getText() {
        return text;
    }
}
