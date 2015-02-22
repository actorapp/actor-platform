package im.actor.gwt.app;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

import org.timepedia.exporter.client.ExporterUtil;

import im.actor.gwt.app.ui.JsDialogList;

public class ActorMessenger implements EntryPoint {

    public void onModuleLoad() {
        ExporterUtil.exportAll();
        GWT.create(JsDialogList.class);
        onAppLoaded();
    }

    public native void onAppLoaded()/*-{
        if ($wnd.jsAppLoaded) $wnd.jsAppLoaded();
    }-*/;
}
