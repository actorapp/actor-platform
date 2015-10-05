package im.actor.core.i18n;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.runtime.Assets;

public class LocaleLoader {

    public static HashMap<String, String> loadPropertiesFile(String name) {
        String fileContent = Assets.loadAsset(name);
        HashMap<String, String> res = new HashMap<String, String>();
        String[] lines = linesWorkaround(fileContent);
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] lineVal = valuesWorkaround(line);
            if (lineVal.length == 2) {
                res.put(lineVal[0], lineVal[1]);
            }
        }
        return res;
    }

    static String[] linesWorkaround(String s) {
        ArrayList<String> res = new ArrayList<String>();
        int index;
        while ((index = s.indexOf("\n")) >= 0) {
            res.add(s.substring(0, index));
            s = s.substring(index + 1);
        }
        res.add(s);
        return res.toArray(new String[res.size()]);
    }

    static String[] valuesWorkaround(String s) {
        int index;
        if ((index = s.indexOf("=")) >= 0) {
            return new String[]{s.substring(0, index),
                    s.substring(index + 1)};
        }

        return new String[]{s};
    }
}
