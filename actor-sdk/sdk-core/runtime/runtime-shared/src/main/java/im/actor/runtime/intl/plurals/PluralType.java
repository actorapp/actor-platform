package im.actor.runtime.intl.plurals;

public abstract class PluralType {

    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int FEW = 3;
    public static final int MANY = 4;
    public static final int OTHER = 5;

    public static String toType(int key) {
        String pluralKey;
        switch (key) {
            case PluralType.ZERO:
                pluralKey = "zero";
                break;
            case PluralType.ONE:
                pluralKey = "one";
                break;
            case PluralType.TWO:
                pluralKey = "two";
                break;
            case PluralType.FEW:
                pluralKey = "few";
                break;
            case PluralType.MANY:
                pluralKey = "many";
                break;
            default:
            case PluralType.OTHER:
                pluralKey = "other";
                break;
        }
        return pluralKey;
    }
}
