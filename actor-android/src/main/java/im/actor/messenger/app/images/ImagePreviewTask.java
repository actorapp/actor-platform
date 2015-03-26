package im.actor.messenger.app.images;

import im.actor.images.loading.AbsTask;

import im.actor.model.entity.Message;

/**
 * Created by ex3ndr on 05.09.14.
 */
public class ImagePreviewTask extends AbsTask {
    private int type;
    private int id;
    private Message message;

    public ImagePreviewTask(int type, int id, Message message) {
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

    public Message getMessage() {
        return message;
    }

    @Override
    public String getKey() {
        return "react:" + message.getRid();
    }
}
