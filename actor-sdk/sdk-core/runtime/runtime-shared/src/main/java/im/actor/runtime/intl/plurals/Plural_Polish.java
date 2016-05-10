package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Polish language:
 * <p>
 * Locales: pl
 * <p>
 * Languages:
 * - Polish (pl)
 * <p>
 * Rules:
 * one → n is 1;
 * few → n mod 10 in 2..4 and n mod 100 not in 12..14 and n mod 100 not in 22..24;
 * other → everything else (fractions)
 */
public class Plural_Polish implements PluralEngine {

    @Override
    public int getPluralType(int value) {
        int rem100 = value % 100;
        int rem10 = value % 10;

        if (value == 1) {
            return PluralType.ONE;
        } else if (rem10 >= 2 && rem10 <= 4 && !(rem100 >= 12 && rem100 <= 14)) {
            return PluralType.FEW;
        } else {
            return PluralType.OTHER;
        }
    }
}
