/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.util.ArrayList;

import im.actor.model.api.TextExMarkdown;
import im.actor.model.api.TextMessage;
import im.actor.model.api.TextMessageEx;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

public class TextContent extends AbsContent {

    public static TextContent create(String text, String markDownText, ArrayList<Integer> mentions) {
        return new TextContent(new ContentRemoteContainer(new TextMessage(text,
                mentions, markDownText == null || markDownText.isEmpty() ? null : new TextExMarkdown(markDownText))));
    }

    private String text;
    private ArrayList<Integer> mentions;
    private TextMessageEx textMessageEx;

    public TextContent(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);
        text = ((TextMessage) remoteContainer.getMessage()).getText();
        mentions = (ArrayList<Integer>) ((TextMessage) remoteContainer.getMessage()).getMentions();
        textMessageEx = ((TextMessage) remoteContainer.getMessage()).getExt();
    }

    public ArrayList<Integer> getMentions() {
        return mentions;
    }

    public String getText() {
        return text;
    }

    public TextMessageEx getTextMessageEx() {
        return textMessageEx;
    }
}
