/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;

import org.timepedia.exporter.client.ExporterUtil;

import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.js.entity.JsContact;
import im.actor.core.js.entity.JsDialog;
import im.actor.core.js.entity.JsMessage;
import im.actor.core.js.providers.Assets;
import im.actor.runtime.js.JsAssetsProvider;
import im.actor.runtime.js.JsEngineProvider;

public class GwtEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        ExporterUtil.exportAll();
        JsAssetsProvider.registerBundle(Assets.INSTANCE);
        JsEngineProvider.registerEntity(Contact.ENTITY_NAME, JsContact.CONVERTER);
        JsEngineProvider.registerEntity(Dialog.ENTITY_NAME, JsDialog.CONVERTER);
        JsEngineProvider.registerEntity(Message.ENTITY_NAME, JsMessage.CONVERTER);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onAppLoaded();
            }
        });
    }

    public native void onAppLoaded()/*-{
        if ($wnd.jsAppLoaded) $wnd.jsAppLoaded();
    }-*/;
}
