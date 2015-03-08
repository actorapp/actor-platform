package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.bser.DataInput;

import java.io.IOException;

/**
 * Created by ex3ndr on 09.02.15.
 */
public abstract class AbsContent extends BserObject {

    public static AbsContent contentFromBytes(byte[] data) throws IOException {
        BserValues reader = new BserValues(BserParser.deserialize(new DataInput(data, 0, data.length)));
        ContentType type = typeFromValue(reader.getInt(1));
        switch (type) {
            case TEXT:
                return TextContent.textFromBytes(data);
            case DOCUMENT:
                return DocumentContent.docFromBytes(data);
            case DOCUMENT_PHOTO:
                return PhotoContent.photoFromBytes(data);
            case DOCUMENT_VIDEO:
                return VideoContent.videoFromBytes(data);
            case SERVICE:
                return ServiceContent.serviceFromBytes(data);
            case SERVICE_REGISTERED:
                return ServiceUserRegistered.fromBytes(data);
            case SERVICE_CREATED:
                return ServiceGroupCreated.fromBytes(data);
            case SERVICE_TITLE:
                return ServiceGroupTitleChanged.fromBytes(data);
            case SERVICE_AVATAR:
                return ServiceGroupAvatarChanged.fromBytes(data);
            case SERVICE_ADDED:
                return ServiceGroupUserAdded.fromBytes(data);
            case SERVICE_KICKED:
                return ServiceGroupUserKicked.fromBytes(data);
            case SERVICE_LEAVE:
                return ServiceGroupUserLeave.fromBytes(data);
            default:
                throw new IOException("Unknown type");
        }
    }

    @Override
    public void parse(BserValues values) throws IOException {

    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, getContentType().getValue());
    }

    protected enum ContentType {
        TEXT(1),
        DOCUMENT(2),
        DOCUMENT_PHOTO(3),
        DOCUMENT_VIDEO(4),
        SERVICE(5),
        SERVICE_CREATED(6),
        SERVICE_AVATAR(7),
        SERVICE_TITLE(8),
        SERVICE_ADDED(9),
        SERVICE_KICKED(10),
        SERVICE_LEAVE(11),
        SERVICE_REGISTERED(12);

        int value;

        ContentType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }


    }

    protected static ContentType typeFromValue(int val) {
        switch (val) {
            default:
            case 1:
                return ContentType.TEXT;
            case 2:
                return ContentType.DOCUMENT;
            case 3:
                return ContentType.DOCUMENT_PHOTO;
            case 4:
                return ContentType.DOCUMENT_VIDEO;
            case 5:
                return ContentType.SERVICE;
            case 6:
                return ContentType.SERVICE_CREATED;
            case 7:
                return ContentType.SERVICE_AVATAR;
            case 8:
                return ContentType.SERVICE_TITLE;
            case 9:
                return ContentType.SERVICE_ADDED;
            case 10:
                return ContentType.SERVICE_KICKED;
            case 11:
                return ContentType.SERVICE_LEAVE;
            case 12:
                return ContentType.SERVICE_REGISTERED;
        }
    }

    protected abstract ContentType getContentType();
}