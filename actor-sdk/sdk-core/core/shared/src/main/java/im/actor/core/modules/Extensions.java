package im.actor.core.modules;

import java.util.ArrayList;

import im.actor.core.Extension;

public class Extensions extends AbsModule {

    private ArrayList<ExtensionRef> extensionRefs = new ArrayList<ExtensionRef>();

    public Extensions(ModuleContext context) {
        super(context);
    }

    public void registerExtension(String key, Extension extension) {
        extensionRefs.add(new ExtensionRef(key, extension));
    }

    public Extension findExtension(String key) {
        for (ExtensionRef r : extensionRefs) {
            if (r.getKey().equals(key)) {
                return r.getExtension();
            }
        }
        return null;
    }

    public void registerExtensions() {
        for (ExtensionRef r : extensionRefs) {
            r.getExtension().registerExtension(context());
        }
    }

    public void runExtensions() {
        for (ExtensionRef r : extensionRefs) {
            r.getExtension().runExtension();
        }
    }

    private class ExtensionRef {
        private String key;
        private Extension extension;

        public ExtensionRef(String key, Extension extension) {
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
}
