package im.actor.runtime.intl.plurals;

/**
 * Plural rules for the following locales and languages
 * <p>
 * Locales: hr ru sr uk be bs sh
 * <p>
 * Languages:
 * - Belarusian (br)
 * - Bosnian (bs)
 * - Croatian (hr)
 * - Russian (ru)
 * - Serbo-Croatian (sh)
 * - Serbian (sr)
 * - Ukrainian (uk)
 * <p>
 * Rules:
 * one → n mod 10 is 1 and n mod 100 is not 11;
 * few → n mod 10 in 2..4 and n mod 100 not in 12..14;
 * many → n mod 10 is 0 or n mod 10 in 5..9 or n mod 100 in 11..14;
 * other → everything else (fractions)
 */
public class Plural_Balkan implements PluralEngine {
    @Override
    public int getPluralType(int value) {

        int rem100 = value % 100;
        int rem10 = value % 10;

        if (rem10 == 1 && rem100 != 11) {
            return PluralType.ONE;
        } else if (rem10 >= 2 && rem10 <= 4 && !(rem100 >= 12 && rem100 <= 14)) {
            return PluralType.FEW;
        } else if ((rem10 == 0 || (rem10 >= 5 && rem10 <= 9) || (rem100 >= 11 && rem100 <= 14))) {
            return PluralType.MANY;
        } else {
            return PluralType.OTHER;
        }
    }
}
