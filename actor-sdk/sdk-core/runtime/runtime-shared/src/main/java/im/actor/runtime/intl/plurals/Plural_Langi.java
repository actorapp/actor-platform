package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Langi language:
 * <p>
 * Locales: lag
 * <p>
 * Languages:
 * - Langi (lag)
 * <p>
 * Rules:
 * zero → n is 0;
 * one → n within 0..2 and n is not 0 and n is not 2;
 * other → everything else
 */
public class Plural_Langi implements PluralEngine {
    @Override
    public int getPluralType(int value) {
        if (value == 0) {
            return PluralType.ZERO;
        } else if (value > 0 && value < 2) {
            return PluralType.ONE;
        } else {
            return PluralType.OTHER;
        }
    }
}
