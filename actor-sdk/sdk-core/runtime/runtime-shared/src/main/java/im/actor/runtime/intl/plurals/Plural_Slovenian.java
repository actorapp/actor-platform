package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Slovenian language:
 * <p>
 * Locales: sl
 * <p>
 * Languages:
 * - Slovenian (sl)
 * <p>
 * Rules:
 * one → n mod 100 is 1;
 * two → n mod 100 is 2;
 * few → n mod 100 in 3..4;
 * other → everything else
 */
public class Plural_Slovenian implements PluralEngine {
    @Override
    public int getPluralType(int value) {

        int rem100 = value % 100;

        if (rem100 == 1) {
            return PluralType.ONE;
        } else if (rem100 == 2) {
            return PluralType.TWO;
        } else if (rem100 >= 3 && rem100 <= 4) {
            return PluralType.FEW;
        } else {
            return PluralType.OTHER;
        }
    }
}
