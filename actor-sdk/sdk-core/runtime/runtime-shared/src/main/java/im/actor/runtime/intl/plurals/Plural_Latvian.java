package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Latvian language:
 * <p>
 * Locales: lv
 * <p>
 * Languages:
 * - Latvian (lv)
 * <p>
 * Rules:
 * zero → n is 0;
 * one → n mod 10 is 1 and n mod 100 is not 11;
 * other → everything else
 */
public class Plural_Latvian implements PluralEngine {

    @Override
    public int getPluralType(int value) {
        if (value == 0) {
            return PluralType.ZERO;
        } else if (value % 10 == 1 && value % 100 != 11) {
            return PluralType.ONE;
        } else {
            return PluralType.OTHER;
        }
    }
}
