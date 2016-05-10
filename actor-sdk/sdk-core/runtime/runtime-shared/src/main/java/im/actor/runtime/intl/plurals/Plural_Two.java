package im.actor.runtime.intl.plurals;

/**
 * Plural rules for the following locales and languages:
 * <p>
 * Locales: ga se sma smi smj smn sms
 * <p>
 * Languages:
 * Irish (ga)
 * Northern Sami (se)
 * Southern Sami (sma)
 * Sami Language (smi)
 * Lule Sami (smj)
 * Inari Sami (smn)
 * Skolt Sami (sms)
 * <p>
 * Rules:
 * one → n is 1;
 * two → n is 2;
 * other → everything else
 */
public class Plural_Two implements PluralEngine {
    @Override
    public int getPluralType(int value) {
        if (value == 1) {
            return PluralType.ONE;
        } else if (value == 2) {
            return PluralType.TWO;
        } else {
            return PluralType.OTHER;
        }
    }
}
