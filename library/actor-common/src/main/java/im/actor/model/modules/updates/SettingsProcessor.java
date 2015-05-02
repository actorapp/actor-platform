package im.actor.model.modules.updates;

import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;

/**
 * Created by ex3ndr on 23.04.15.
 */
public class SettingsProcessor extends BaseModule {

    public SettingsProcessor(Modules modules) {
        super(modules);
    }

    public void onSettingsChanged(String key, String value) {
        modules().getPreferences().putString(key, value);
    }
}
