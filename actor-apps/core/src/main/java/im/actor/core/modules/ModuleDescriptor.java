package im.actor.core.modules;

import java.util.ArrayList;
import java.util.List;

public class ModuleDescriptor<T extends Module> {

    public static class Builder<T extends Module> {

        private boolean isLoginRequired = true;
        private ArrayList<Integer> updates = new ArrayList<Integer>();
        private ModuleCreator<T> creator;
        private String moduleKey;

        public Builder() {

        }

        public Builder setKey(String moduleKey) {
            this.moduleKey = moduleKey;
            return this;
        }

        public Builder setCreator(ModuleCreator<T> creator) {
            this.creator = creator;
            return this;
        }

        public Builder setLoginRequired(boolean isLoginRequired) {
            this.isLoginRequired = isLoginRequired;
            return this;
        }

        public Builder addUpdate(int id) {
            updates.add(id);
            return this;
        }

        public ModuleDescriptor<T> build() {
            return new ModuleDescriptor<T>(isLoginRequired, updates, creator);
        }
    }


    private boolean isLoginRequired = false;
    private ArrayList<Integer> updates = new ArrayList<Integer>();
    private ModuleCreator<T> creator;

    public ModuleDescriptor(boolean isLoginRequired, ArrayList<Integer> updates,
                            ModuleCreator<T> creator) {
        this.isLoginRequired = isLoginRequired;
        this.updates = updates;
        this.creator = creator;
    }

    public List<Integer> getUpdates() {
        return updates;
    }

    public boolean isLoginRequired() {
        return isLoginRequired;
    }
}
