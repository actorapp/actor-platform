/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;

public class SettingsProcessor extends AbsModule {

    public SettingsProcessor(ModuleContext modules) {
        super(modules);
    }

    public void onSettingsChanged(String key, String value) {
        context().getSettingsModule().onUpdatedSetting(key, value);
    }
}
