package im.actor.runtime.intl.plurals;

/**
 * Plural rules for the following locales and languages:
 * <p>
 * Locales: ro mo
 * <p>
 * Languages:
 * Moldavian (mo)
 * Romanian (ro)
 * <p>
 * Rules:
 * one → n is 1;
 * few → n is 0 || n is not 1 && n mod 100 in 1..19;
 * other → everything else
 */
public class Plural_Romanian implements PluralEngine {
    @Override
    public int getPluralType(int value) {
        int rem100 = value % 100;

        if (value == 1) {
            return PluralType.ONE;
        } else if ((value == 0 || (rem100 >= 1 && rem100 <= 19))) {
            return PluralType.FEW;
        } else {
            return PluralType.OTHER;
        }
    }
}
