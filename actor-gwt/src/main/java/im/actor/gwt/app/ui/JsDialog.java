package im.actor.gwt.app.ui;

import com.google.gwt.core.client.JavaScriptObject;

import org.timepedia.exporter.client.Exportable;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JsDialog extends JavaScriptObject implements Exportable {

    public static native JsDialog create(int peerId, int peerType, String title,
                                         String text)/*-{
        return {peerId: peerId, title: title, text: text};
    }-*/;

    protected JsDialog() {

    }

    public final native String getText()/*-{ return this.text; }-*/;

    public final native int getPeerId() /*-{
        return this.peerId;
    }-*/;

    public final native String getTitle() /*-{
        return this.title;
    }-*/;
}
