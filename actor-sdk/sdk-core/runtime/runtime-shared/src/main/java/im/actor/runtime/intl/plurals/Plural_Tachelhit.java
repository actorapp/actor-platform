package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Tachelhit language:
 * <p>
 * Locales: shi
 * <p>
 * Languages:
 * - Tachelhit (shi)
 * <p>
 * Rules:
 * one → n within 0..1;
 * few → n in 2..10;
 * other → everything else
 */
public class Plural_Tachelhit implements PluralEngine {
    @Override
    public int getPluralType(int value) {
        if (value >= 0 && value <= 1) {
            return PluralType.ONE;
        } else if (value >= 2 && value <= 10) {
            return PluralType.FEW;
        } else {
            return PluralType.OTHER;
        }
    }
}
