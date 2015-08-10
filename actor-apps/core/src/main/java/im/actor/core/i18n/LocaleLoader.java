package im.actor.core.i18n;

import java.util.HashMap;

import im.actor.runtime.Assets;

public class LocaleLoader {

    public static HashMap<String, String> loadPropertiesFile(String name) {
        String fileContent = Assets.loadAsset(name);
        HashMap<String, String> res = new HashMap<String, String>();
        String[] lines = fileContent.split("\n");
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] lineVal = line.split("=", 2);
            if (lineVal.length == 2) {
                res.put(lineVal[0], lineVal[1]);
            }
        }
        return res;
    }
}
