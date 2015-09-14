package im.actor.core;

import java.util.HashMap;

import im.actor.core.modules.ModuleContext;
import im.actor.runtime.util.ClassCreator;

/**
 * Extensions index
 */
public class Extensions {

    // Add here Your extensions
    private static final ClassCreator<Extension>[] APP_EXTENSIONS = new ClassCreator[]{
            ExampleExtension.CREATOR
    };

    /**
     * Context for extensions
     */
    private ModuleContext context;

    /**
     * Created extensions
     */
    private Extension[] extensions;

    /**
     * Map of extension from key to extension
     */
    private HashMap<String, Extension> extensionsMap;

    /**
     * Create extensions module
     *
     * @param context module context
     */
    public Extensions(ModuleContext context) {
        this.context = context;
    }

    /**
     * Creating and registering extensions
     */
    public void registerExtensions() {
        extensionsMap = new HashMap<String, Extension>();
        extensions = new Extension[APP_EXTENSIONS.length];
        for (int i = 0; i < extensions.length; i++) {
            extensions[i] = APP_EXTENSIONS[i].newInstance();
            extensionsMap.put(extensions[i].getExtensionKey(), extensions[i]);
        }
        for (int i = 0; i < extensions.length; i++) {
            extensions[i].registerExtension(context);
        }
    }

    /**
     * Running extensions
     */
    public void runExtensions() {
        for (int i = 0; i < extensions.length; i++) {
            extensions[i].runExtension();
        }
    }

    /**
     * Finding extension
     *
     * @param key key of extension
     * @return founded extension, null if not found
     */
    public Extension findExtension(String key) {
        return extensionsMap.get(key);
    }
}
