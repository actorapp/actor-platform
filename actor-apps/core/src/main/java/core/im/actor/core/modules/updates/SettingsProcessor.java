/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import im.actor.core.modules.BaseModule;
import im.actor.core.modules.Modules;

public class SettingsProcessor extends BaseModule {

    public SettingsProcessor(Modules modules) {
        super(modules);
    }

    public void onSettingsChanged(String key, String value) {
        modules().getSettings().onUpdatedSetting(key, value);
    }
}
