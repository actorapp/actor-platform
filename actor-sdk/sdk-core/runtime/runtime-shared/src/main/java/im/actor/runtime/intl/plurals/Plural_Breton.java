package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Breton language:
 * <p>
 * Locales: br
 * <p>
 * Languages:
 * - Breton (br)
 * <p>
 * Rules:
 * zero → n is 0;
 * one → n is 1;
 * two → n is 2;
 * few → n is 3;
 * many → n is 6;
 * other → everything else
 */
public class Plural_Breton implements PluralEngine {

    @Override
    public int getPluralType(int value) {
        if (value == 0) {
            return PluralType.ZERO;
        } else if (value == 1) {
            return PluralType.ONE;
        } else if (value == 2) {
            return PluralType.TWO;
        } else if (value == 3) {
            return PluralType.FEW;
        } else if (value == 6) {
            return PluralType.MANY;
        } else {
            return PluralType.OTHER;
        }
    }
}
