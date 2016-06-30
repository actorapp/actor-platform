package im.actor.runtime.intl.plurals;

import java.util.HashMap;
import java.util.Map;

public class PluralFactory {

    private static Map<String, PluralEngine> ALL_ENGINES = new HashMap<>();

    static {
        addRules(new String[]{"bem", "brx", "da", "de", "el", "en", "eo", "es", "et", "fi", "fo", "gl", "he", "iw", "it", "nb",
                "nl", "nn", "no", "sv", "af", "bg", "bn", "ca", "eu", "fur", "fy", "gu", "ha", "is", "ku",
                "lb", "ml", "mr", "nah", "ne", "om", "or", "pa", "pap", "ps", "so", "sq", "sw", "ta", "te",
                "tk", "ur", "zu", "mn", "gsw", "chr", "rm", "pt"}, new Plural_One());
        addRules(new String[]{"cs", "sk"}, new Plural_Czech());
        addRules(new String[]{"ff", "fr", "kab"}, new Plural_French());
        addRules(new String[]{"hr", "ru", "sr", "uk", "be", "bs", "sh"}, new Plural_Balkan());
        addRules(new String[]{"lv"}, new Plural_Latvian());
        addRules(new String[]{"lt"}, new Plural_Lithuanian());
        addRules(new String[]{"pl"}, new Plural_Polish());
        addRules(new String[]{"ro", "mo"}, new Plural_Romanian());
        addRules(new String[]{"sl"}, new Plural_Slovenian());
        addRules(new String[]{"ar"}, new Plural_Arabic());
        addRules(new String[]{"mk"}, new Plural_Macedonian());
        addRules(new String[]{"cy"}, new Plural_Welsh());
        addRules(new String[]{"br"}, new Plural_Breton());
        addRules(new String[]{"lag"}, new Plural_Langi());
        addRules(new String[]{"shi"}, new Plural_Tachelhit());
        addRules(new String[]{"mt"}, new Plural_Maltese());
        addRules(new String[]{"ga", "se", "sma", "smi", "smj", "smn", "sms"}, new Plural_Two());
        addRules(new String[]{"ak", "am", "bh", "fil", "tl", "guw", "hi", "ln", "mg", "nso", "ti", "wa"}, new Plural_Zero());
        addRules(new String[]{"az", "bm", "fa", "ig", "hu", "ja", "kde", "kea", "ko", "my", "ses", "sg", "to",
                "tr", "vi", "wo", "yo", "zh", "bo", "dz", "id", "jv", "ka", "km", "kn", "ms", "th"}, new Plural_None());

    }

    private static void addRules(String[] languages, PluralEngine rules) {
        for (String language : languages) {
            ALL_ENGINES.put(language, rules);
        }
    }

    public static PluralEngine getPluralForLanguage(String language) {
        return ALL_ENGINES.get(language.toLowerCase());
    }
}
