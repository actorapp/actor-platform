/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.core.api.TextExMarkdown;
import im.actor.core.api.TextMessage;
import im.actor.core.api.TextMessageEx;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class TextContent extends AbsContent {

    @NotNull
    public static TextContent create(@NotNull String text, @Nullable String markDownText, @Nullable ArrayList<Integer> mentions) {
        if (mentions == null) {
            mentions = new ArrayList<Integer>();
        }

        return new TextContent(new ContentRemoteContainer(
                new TextMessage(
                        text,
                        mentions,
                        markDownText == null || markDownText.isEmpty()
                                ? null
                                : new TextExMarkdown(markDownText))));
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
