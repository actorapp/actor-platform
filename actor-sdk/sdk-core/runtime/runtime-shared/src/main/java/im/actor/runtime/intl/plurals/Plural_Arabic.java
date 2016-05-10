package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Arabic language
 * <p>
 * Locales: ar
 * <p>
 * Languages:
 * - Arabic (ar)
 * <p>
 * Rules:
 * zero → n is 0;
 * one → n is 1;
 * two → n is 2;
 * few → n mod 100 in 3..10;
 * many → n mod 100 in 11..99;
 * other → everything else
 */
public class Plural_Arabic implements PluralEngine {
    @Override
    public int getPluralType(int value) {

        int rem100 = value % 100;

        if (value == 0) {
            return PluralType.ZERO;
        } else if (value == 1) {
            return PluralType.ONE;
        } else if (value == 2) {
            return PluralType.TWO;
        } else if (rem100 >= 3 && rem100 <= 10) {
            return PluralType.FEW;
        } else if (rem100 >= 11 && rem100 <= 99) {
            return PluralType.MANY;
        } else {
            return PluralType.OTHER;
        }
    }
}
