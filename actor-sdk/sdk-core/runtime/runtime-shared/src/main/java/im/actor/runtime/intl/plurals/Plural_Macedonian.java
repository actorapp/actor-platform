package im.actor.runtime.intl.plurals;

/**
 * Plural rules for Macedonian language
 * <p>
 * Locales: mk
 * <p>
 * Languages:
 * - Macedonian (mk)
 * <p>
 * Rules:
 * one → n mod 10 is 1 and n is not 11;
 * other → everything else
 */
public class Plural_Macedonian implements PluralEngine {
    @Override
    public int getPluralType(int value) {
        if (value % 10 == 1 && value != 11) {
            return PluralType.ONE;
        } else {
            return PluralType.OTHER;
        }
    }
}
