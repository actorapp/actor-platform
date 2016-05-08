package im.actor.core.i18n;

import java.util.HashMap;

import im.actor.runtime.Assets;

public class LocaleLoader {

    public static HashMap<String, String> loadPropertiesFile(String name) {
        String fileContent = Assets.loadAsset(name);
        HashMap<String, String> res = new HashMap<>();
        processLines(res, fileContent);
        return res;
    }

    private static void processLines(HashMap<String, String> res, String s) {
        for (String l : s.split("\\n")) {
            if (l.startsWith("#")) {
                continue;
            }
            processLine(res, l);
        }
    }

    private static void processLine(HashMap<String, String> res, String s) {
        String[] parts = s.split("=", 2);
        if (parts.length == 2) {
            res.put(parts[0], parts[1]);
        }
    }
}
