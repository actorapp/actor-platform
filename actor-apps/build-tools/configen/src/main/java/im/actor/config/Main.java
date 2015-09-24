package im.actor.config;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {

        System.out.println("Config builder for Actor Apps");

        if (args.length != 5) {
            System.out.println("USAGE: configen <source_config> android|ios|web <app> <variant> <dest_path>");
            return;
        }

        String sourceFileName = args[0];
        String destFileName = args[4];
        String platform = args[1];
        String app = args[2];
        String variant = args[3];

        Yaml yaml = new Yaml();
        Map<String, Object> appData = (Map<String, Object>) ((Map) ((Map) yaml.load(new FileInputStream(sourceFileName)))
                .get("apps"))
                .get(app);

        JSONObject res = new JSONObject();

        for (String key : appData.keySet()) {
            if ("android".equals(key)) {
                continue;
            }
            if ("ios".equals(key)) {
                continue;
            }
            if ("web".equals(key)) {
                continue;
            }

            // Ignore build section
            if ("build".equals(key)) {
                continue;
            }

            res.put(key, convertToJson(appData.get(key)));
        }

        if (appData.containsKey(platform)) {

            // Platform values
            Map<String, Object> platformData = (Map<String, Object>) appData.get(platform);

            for (String key : platformData.keySet()) {
                if ("variants".equals(key)) {
                    continue;
                }

                // Override keys
                res.remove(key);
                res.put(key, convertToJson(platformData.get(key)));
            }

            // Variant values
            if (platformData.containsKey("variants")) {
                Map<String, Object> variants = (Map<String, Object>) platformData.get("variants");
                if (variants.containsKey(variant)) {
                    Map<String, Object> variantData = (Map<String, Object>) variants.get(variant);

                    for (String key : variantData.keySet()) {
                        if ("build".equals(key)) {
                            continue;
                        }

                        // Override keys
                        res.remove(key);
                        res.put(key, convertToJson(variantData.get(key)));
                    }
                }
            }
        }

        JSonWriter writer = new JSonWriter();
        res.writeJSONString(writer);
        String data = writer.toString();

        FileWriter fileWriter = new FileWriter(destFileName);
        fileWriter.write(data);
        fileWriter.close();
    }

    protected static Object convertToJson(Object src) {
        if (src instanceof Map) {
            JSONObject res = new JSONObject();
            for (String s : ((Map<String, Object>) src).keySet()) {
                res.put(s, ((Map<String, Object>) src).get(s));
            }
            return res;
        } else if (src instanceof String) {
            return src;
        } else if (src instanceof Integer) {
            return src;
        } else if (src instanceof List) {
            JSONArray res = new JSONArray();
            for (Object o : (List) src) {
                res.add(convertToJson(o));
            }
            return res;
        }

        throw new RuntimeException("type: " + src.getClass());
    }
}
