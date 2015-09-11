package im.actor.core;

import im.actor.core.modules.ModuleContext;

class DumpExtension {
    private ModuleContext context;

    public DumpExtension(Messenger messenger) {
        this.context = messenger.getModuleContext();

        // Now you can easily access to all internals of Messenger
    }
}
