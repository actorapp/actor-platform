package im.actor.runtime.intl;

import java.util.HashMap;

import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

public class IntlEngine {

    private HashMap<String, String> keys;

    public IntlEngine(String localization) throws JSONException {
        this.keys = new HashMap<>();
        traverseObject(new JSONObject(localization), "");
    }

    private void traverseObject(JSONObject src, String prefix) throws JSONException {
        for (String s : src.keySet()) {
            Object itm = src.get(s);
            if (itm instanceof String) {
                keys.put(prefix + s, (String) itm);
            } else if (itm instanceof JSONObject) {
                traverseObject((JSONObject) itm, prefix + s + ".");
            } else {
                throw new RuntimeException("Unexpected object: " + itm);
            }
        }
    }
}