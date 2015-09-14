package im.actor.core;

import im.actor.core.modules.ModuleContext;

/**
 * Platform Extension interface
 */
public interface Extension {

    /**
     * Performing registration of extension before Messenger run
     *
     * @param context Module context
     */
    void registerExtension(ModuleContext context);

    /**
     * Running extension
     */
    void runExtension();
}
