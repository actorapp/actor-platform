/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.updates;

import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;

public class SettingsProcessor extends BaseModule {

    public SettingsProcessor(Modules modules) {
        super(modules);
    }

    public void onSettingsChanged(String key, String value) {
        modules().getSettings().onUpdatedSetting(key, value);
    }
}
