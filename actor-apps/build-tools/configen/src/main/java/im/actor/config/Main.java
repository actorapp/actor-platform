package im.actor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        System.out.println("Config builder for Actor Apps");

        if (args.length != 3 && args.length != 4) {
            System.out.println("USAGE: configen <source_config> [optional_config] <dest_file> android|ios|web");
            return;
        }

        String sourceFileName = args[0];
        String altFileName = args.length == 4 ? args[1] : null;
        String destFileName = args[1 + (args.length - 3)];
        String type = args[2 + (args.length - 3)];

        Config config = ConfigFactory.parseFile(new File(sourceFileName));
        Config configAlt = null;
        if (altFileName != null) {
            configAlt = ConfigFactory.parseFile(new File(altFileName));
        }

        // Build Android config

        MobileConfig android = new MobileConfig();
        MobileConfig ios = new MobileConfig();
        WebConfig web = new WebConfig();

        // Endpoints
        List<String> str = config.getStringList("server_endpoints");
        for (String s : str) {
            URI uri = URI.create(s);
            if (uri.getScheme().equals("tls") || uri.getScheme().equals("tcp")) {
                android.getEndpoints().add(s);
                ios.getEndpoints().add(s);
            } else {
                web.getEndpoints().add(s);
            }
        }
        if (android.getEndpoints().size() == 0) {
            throw new RuntimeException("No Mobile endpoints specified");
        }

        ios.setMixpanel(getString(config, configAlt, "mixpanel.ios"));
        android.setMixpanel(getString(config, configAlt, "mixpanel.android"));

        ios.setHockeyApp(getString(config, configAlt, "hockeyapp.ios"));
        android.setHockeyApp(getString(config, configAlt, "hockeyapp.android"));

        ios.setMint(getString(config, configAlt, "mint.ios"));
        android.setMint(getString(config, configAlt, "mint.android"));

        // Save Config
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        if (type.equals("android")) {
            mapper.writeValue(new File(destFileName), android);
        } else if (type.equals("ios")) {
            mapper.writeValue(new File(destFileName), ios);
        } else if (type.equals("web")) {
            mapper.writeValue(new File(destFileName), web);
        } else {
            throw new RuntimeException("Unknown type: " + type);
        }
    }

    protected static String getString(Config config, Config altConfig, String path) {
        if (altConfig != null) {
            if (altConfig.hasPath(path)) {
                if (!altConfig.getIsNull(path)) {
                    return altConfig.getString(path);
                } else {
                    return null;
                }
            }
        }
        if (config.hasPath(path)) {
            if (!config.getIsNull(path)) {
                return config.getString(path);
            }
        }
        return null;
    }
}
