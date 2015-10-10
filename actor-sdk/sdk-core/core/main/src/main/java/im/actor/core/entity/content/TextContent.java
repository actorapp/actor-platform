/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.core.api.ApiTextExMarkdown;
import im.actor.core.api.ApiTextMessage;
import im.actor.core.api.ApiTextMessageEx;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

public class TextContent extends AbsContent {

    @NotNull
    public static TextContent create(@NotNull String text, @Nullable String markDownText, @Nullable ArrayList<Integer> mentions) {
        if (mentions == null) {
            mentions = new ArrayList<Integer>();
        }

        return new TextContent(new ContentRemoteContainer(
                new ApiTextMessage(
                        text,
                        mentions,
                        markDownText == null || markDownText.isEmpty()
                                ? null
                                : new ApiTextExMarkdown(markDownText))));
    }

    @Property("readonly, nonatomic")
    private final String text;
    @Property("readonly, nonatomic")
    private final ApiTextMessageEx textMessageEx;

    private final ArrayList<Integer> mentions;

    public TextContent(ContentRemoteContainer remoteContainer) {
        super(remoteContainer);
        text = ((ApiTextMessage) remoteContainer.getMessage()).getText();
        mentions = (ArrayList<Integer>) ((ApiTextMessage) remoteContainer.getMessage()).getMentions();
        textMessageEx = ((ApiTextMessage) remoteContainer.getMessage()).getExt();
    }

    public ArrayList<Integer> getMentions() {
        return mentions;
    }

    public String getText() {
        return text;
    }

    public ApiTextMessageEx getTextMessageEx() {
        return textMessageEx;
    }
}
