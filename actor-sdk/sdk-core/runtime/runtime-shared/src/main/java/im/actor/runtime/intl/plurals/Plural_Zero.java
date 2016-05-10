package im.actor.runtime.intl.plurals;

/**
 * Plural rules for the following locales and languages:
 * <p>
 * Locales: ak am bh fil tl guw hi ln mg nso ti wa
 * <p>
 * Languages:
 * Akan (ak)
 * Amharic (am)
 * Bihari (bh)
 * Filipino (fil)
 * Gun (guw)
 * Hindi (hi)
 * Lingala (ln)
 * Malagasy (mg)
 * Northern Sotho (nso)
 * Tigrinya (ti)
 * Tagalog (tl)
 * Walloon (wa)
 * <p>
 * Rules:
 * one → n in 0..1;
 * other → everything else
 */
public class Plural_Zero implements PluralEngine {

    @Override
    public int getPluralType(int value) {
        if (value == 0 || value == 1) {
            return PluralType.ONE;
        } else {
            return PluralType.OTHER;
        }
    }
}
