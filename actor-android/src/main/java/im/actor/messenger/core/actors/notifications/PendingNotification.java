package im.actor.messenger.core.actors.notifications;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class PendingNotification extends BserObject {

    private int convType;
    private int convId;
    private long rid;
    private int uid;
    private long date;

    private Type type;

    private int destUid;
    private String text;

    public PendingNotification(int convType, int convId, long rid, int uid, long date, Type type, String text, int destUid) {
        this.convType = convType;
        this.convId = convId;
        this.rid = rid;
        this.uid = uid;
        this.date = date;
        this.type = type;
        this.text = text;
        this.destUid = destUid;
    }

    public PendingNotification() {

    }

    public int getConvType() {
        return convType;
    }

    public int getConvId() {
        return convId;
    }

    public long getRid() {
        return rid;
    }

    public int getUid() {
        return uid;
    }

    public long getDate() {
        return date;
    }

    public Type getType() {
        return type;
    }

    public int getDestUid() {
        return destUid;
    }

    public String getText() {
        return text;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        convType = values.getInt(1);
        convId = values.getInt(2);
        rid = values.getLong(3);
        uid = values.getInt(4);
        date = values.getLong(5);

        destUid = values.optInt(10);
        text = values.optString(11);

        int rawType = values.getInt(12, 0);
        switch (rawType) {
            default:
            case 0:
                type = Type.TEXT;
                break;
            case 1:
                type = Type.PHOTO;
                break;
            case 2:
                type = Type.VIDEO;
                break;
            case 3:
                type = Type.DOCUMENT;
                break;
            case 4:
                type = Type.VOICE;
                break;
            case 5:
                type = Type.USER_REGISTERED;
                break;
            case 6:
                type = Type.GROUP_CREATED;
                break;
            case 7:
                type = Type.GROUP_ADDED;
                break;
            case 8:
                type = Type.GROUP_LEFT;
                break;
            case 9:
                type = Type.GROUP_KICKED;
                break;
            case 10:
                type = Type.USER_DEVICE;
                break;
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, convType);
        writer.writeInt(2, convId);
        writer.writeLong(3, rid);
        writer.writeInt(4, uid);
        writer.writeLong(5, date);

        if (destUid != 0) {
            writer.writeInt(10, destUid);
        }
        if (text != null) {
            writer.writeString(11, text);
        }

        int rawType;
        switch (type) {
            default:
            case TEXT:
                rawType = 0;
                break;
            case PHOTO:
                rawType = 1;
                break;
            case VIDEO:
                rawType = 2;
                break;
            case DOCUMENT:
                rawType = 3;
                break;
            case VOICE:
                rawType = 4;
                break;
            case USER_REGISTERED:
                rawType = 5;
                break;
            case GROUP_CREATED:
                rawType = 6;
                break;
            case GROUP_ADDED:
                rawType = 7;
                break;
            case GROUP_LEFT:
                rawType = 8;
                break;
            case GROUP_KICKED:
                rawType = 9;
                break;
            case USER_DEVICE:
                rawType = 10;
                break;
        }

        writer.writeInt(12, rawType);
    }

    public enum Type {
        TEXT, PHOTO, VIDEO, DOCUMENT, VOICE,
        USER_REGISTERED, USER_DEVICE,
        GROUP_CREATED, GROUP_ADDED, GROUP_LEFT, GROUP_KICKED
    }
}
