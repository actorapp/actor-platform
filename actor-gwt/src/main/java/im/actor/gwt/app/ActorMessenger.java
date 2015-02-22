package im.actor.gwt.app;

import com.google.gwt.core.client.EntryPoint;

import org.timepedia.exporter.client.ExporterUtil;

public class ActorMessenger implements EntryPoint {

    public void onModuleLoad() {
        ExporterUtil.exportAll();
        onAppLoaded();
    }

    public native void onAppLoaded()/*-{
        if ($wnd.jsAppLoaded) $wnd.jsAppLoaded();
    }-*/;
}
