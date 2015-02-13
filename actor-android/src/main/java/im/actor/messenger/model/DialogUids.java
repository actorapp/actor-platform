package im.actor.messenger.model;

import im.actor.messenger.storage.scheme.messages.DialogItem;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class DialogUids {
    public static long getDialogUid(DialogItem item) {
        return item.getId() + ((long) item.getType() << 32);
    }

    public static long getDialogUid(int type, int id) {
        return id + ((long) type << 32);
    }

    public static int getId(long uid) {
        return (int) (uid & 0xFFFFFFFFL);
    }

    public static int getType(long uid) {
        return (int) ((uid >> 32) & 0xFFFFFFFFL);
    }
}
