package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Maltese language:
 * <p>
 * Locales: mt
 * <p>
 * Languages:
 * - Maltese (mt)
 * <p>
 * Rules:
 * one → n is 1;
 * few → n is 0 or n mod 100 in 2..10;
 * many → n mod 100 in 11..19;
 * other → everything else
 */
public class Plural_Maltese implements PluralEngine {
    @Override
    public int getPluralType(int value) {
        int rem100 = value % 100;

        if (value == 1) {
            return PluralType.ONE;
        } else if (value == 0 || (rem100 >= 2 && rem100 <= 10)) {
            return PluralType.FEW;
        } else if (rem100 >= 11 && rem100 <= 19) {
            return PluralType.MANY;
        } else {
            return PluralType.OTHER;
        }
    }
}
