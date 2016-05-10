package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Lithuanian language
 * <p>
 * Locales: lt
 * <p>
 * Languages:
 * - Lithuanian (lt)
 * <p>
 * Rules:
 * one → n mod 10 is 1 and n mod 100 not in 11..19;
 * few → n mod 10 in 2..9 and n mod 100 not in 11..19;
 * other → everything else
 */
public class Plural_Lithuanian implements PluralEngine {

    @Override
    public int getPluralType(int value) {
        int rem100 = value % 100;
        int rem10 = value % 10;

        if (rem10 == 1 && !(rem100 >= 11 && rem100 <= 19)) {
            return PluralType.ONE;
        } else if (rem10 >= 2 && rem10 <= 9 && !(rem100 >= 11 && rem100 <= 19)) {
            return PluralType.FEW;
        } else {
            return PluralType.OTHER;
        }
    }
}
