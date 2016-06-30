package im.actor.runtime.intl.plurals;

/**
 * Plural rules for the following locales and languages
 * <p>
 * Locales: ff fr kab
 * <p>
 * Languages:
 * Fulah (ff)
 * French (fr)
 * Kabyle (kab)
 * <p>
 * Rules:
 * one → n within 0..2 and n is not 2;
 * other → everything else
 */
public class Plural_French implements PluralEngine {
    @Override
    public int getPluralType(int value) {
        if (value >= 0 && value < 2) {
            return PluralType.ONE;
        } else {
            return PluralType.OTHER;
        }
    }
}
