package im.actor.core;

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
        extensions = new Extension[APP_EXTENSIONS.length];
        for (int i = 0; i < extensions.length; i++) {
            extensions[i] = APP_EXTENSIONS[i].newInstance();
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
}
