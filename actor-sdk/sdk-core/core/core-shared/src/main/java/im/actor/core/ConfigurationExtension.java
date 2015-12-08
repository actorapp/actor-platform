package im.actor.core;

public class ConfigurationExtension {

    private String key;
    private Extension extension;

    public ConfigurationExtension(String key, Extension extension) {
        this.key = key;
        this.extension = extension;
    }

    public String getKey() {
        return key;
    }

    public Extension getExtension() {
        return extension;
    }
}
