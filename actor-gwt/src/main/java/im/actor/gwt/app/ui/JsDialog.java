package im.actor.gwt.app.ui;

import com.google.gwt.core.client.JavaScriptObject;

import org.timepedia.exporter.client.Exportable;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JsDialog extends JavaScriptObject implements Exportable {

    public static native JsDialog create(JsPeer peer,
                                         String title,
                                         String avatar,
                                         String placeholder,
                                         String date,
                                         String sender, boolean showSender,
                                         String text, boolean isHighlighted,
                                         String state)/*-{
        return {peer: peer, title: title, text: text, date: date, sender: sender, showSender: showSender,
        isHighlighted: isHighlighted, state:state, avatar: avatar, placeholder:placeholder };
    }-*/;

    protected JsDialog() {
    }
}
