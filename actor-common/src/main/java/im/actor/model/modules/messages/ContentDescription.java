package im.actor.model.modules.messages;

import im.actor.model.entity.Dialog;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.TextContent;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class ContentDescription {

    private Dialog.ContentType contentType;
    private String text;
    private int relatedUid;
    private boolean isSilent;

    public ContentDescription(AbsContent msg) {
        if (msg instanceof TextContent) {
            contentType = Dialog.ContentType.TEXT;
            text = ((TextContent) msg).getText();
        } else {

            throw new RuntimeException("Unknown content type");
        }
    }

    public boolean isSilent() {
        return isSilent;
    }

    public Dialog.ContentType getContentType() {
        return contentType;
    }

    public String getText() {
        return text;
    }

    public int getRelatedUid() {
        return relatedUid;
    }
}
