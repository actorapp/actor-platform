package im.actor.core;

import im.actor.core.modules.ModuleContext;
import im.actor.runtime.util.ClassCreator;

class ExampleExtension implements Extension {

    /**
     * For registering extension implement creator and add it to Extensions class
     */
    public static ClassCreator<ExampleExtension> CREATOR = new ClassCreator<ExampleExtension>() {
        @Override
        public ExampleExtension newInstance() {
            return new ExampleExtension();
        }
    };

    /**
     * Keep reference to context
     */
    private ModuleContext context;

    /**
     * Performing registration of extension before Messenger run
     *
     * @param context Module context
     */
    @Override
    public void registerExtension(ModuleContext context) {
        this.context = context;

        // TODO: Register extension. For example register new content types or update handlers
    }

    /**
     * Running extension
     */
    @Override
    public void runExtension() {

        // TODO: Start extension actors
    }
}
