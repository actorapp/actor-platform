package im.actor.messenger.storage.scheme.messages;

/**
 * Created by ex3ndr on 18.10.14.
 */
public enum MessageState {

    PENDING,
    SENT,
    RECEIVED,
    READ,
    ERROR;

    public static MessageState parse(int val) {
        switch (val) {
            default:
            case 1:
                return PENDING;
            case 2:
                return SENT;
            case 3:
                return READ;
            case 4:
                return ERROR;
            case 5:
                return RECEIVED;
        }
    }

    public static int serialize(MessageState state) {
        switch (state) {
            default:
            case PENDING:
                return 1;
            case SENT:
                return 2;
            case READ:
                return 3;
            case ERROR:
                return 4;
            case RECEIVED:
                return 5;
        }
    }
}
