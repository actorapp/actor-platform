package im.actor.messenger.core.images;

import com.droidkit.images.loading.AbsTask;

import im.actor.messenger.storage.scheme.messages.ConversationMessage;

/**
 * Created by ex3ndr on 05.09.14.
 */
public class ImagePreviewTask extends AbsTask {
    private int type;
    private int id;
    private ConversationMessage message;

    public ImagePreviewTask(int type, int id, ConversationMessage message) {
        this.type = type;
        this.id = id;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public ConversationMessage getMessage() {
        return message;
    }

    @Override
    public String getKey() {
        return "react:" + message.getRid();
    }
}
