package im.actor.gwt.app;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

import org.timepedia.exporter.client.Exporter;

public class ActorMessenger implements EntryPoint {

    public void onModuleLoad() {
        ((Exporter) GWT.create(JsMessenger.class)).export();
        onAppLoaded();
    }

    public native void onAppLoaded()/*-{
        if ($wnd.jsAppLoaded) $wnd.jsAppLoaded();
    }-*/;
}
