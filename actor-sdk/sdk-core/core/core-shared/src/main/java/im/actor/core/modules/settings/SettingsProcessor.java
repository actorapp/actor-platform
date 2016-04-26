/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.settings;

import im.actor.core.api.updates.UpdateParameterChanged;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class SettingsProcessor extends AbsModule implements SequenceProcessor {

    public SettingsProcessor(ModuleContext modules) {
        super(modules);
    }

    public void onSettingsChanged(String key, String value) {
        context().getSettingsModule().onUpdatedSetting(key, value);
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateParameterChanged) {
            onSettingsChanged(
                    ((UpdateParameterChanged) update).getKey(),
                    ((UpdateParameterChanged) update).getValue());
            return Promise.success(null);
        }
        return null;
    }
}
