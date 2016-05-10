package im.actor.runtime.intl.plurals;

/**
 * Plural rules for the following locales and languages
 * <p>
 * Locales: cs sk
 * <p>
 * Languages:
 * - Czech (cs)
 * - Slovak (sk)
 * <p>
 * Rules:
 * one → n is 1;
 * few → n in 2..4;
 * other → everything else
 */
public class Plural_Czech implements PluralEngine {
    @Override
    public int getPluralType(int value) {
        if (value == 1) {
            return PluralType.ONE;
        } else if (value >= 2 && value <= 4) {
            return PluralType.FEW;
        } else {
            return PluralType.OTHER;
        }
    }
}
