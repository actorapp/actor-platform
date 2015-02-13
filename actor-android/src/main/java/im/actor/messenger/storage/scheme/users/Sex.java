package im.actor.messenger.storage.scheme.users;

/**
 * Created by ex3ndr on 18.10.14.
 */
public enum Sex {
    UNKNOWN,
    MALE,
    FEMALE;

    public static int serialize(Sex sex) {
        switch (sex) {
            default:
            case UNKNOWN:
                return 1;
            case MALE:
                return 2;
            case FEMALE:
                return 3;
        }
    }

    public static Sex parse(int value) {
        switch (value) {
            default:
            case 1:
                return UNKNOWN;
            case 2:
                return MALE;
            case 3:
                return FEMALE;
        }
    }
}
