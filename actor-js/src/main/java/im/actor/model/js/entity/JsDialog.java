package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.model.entity.Dialog;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.js.JsMessenger;
import org.timepedia.exporter.client.Exportable;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JsDialog extends JavaScriptObject implements Exportable {

    public static final JsEntityConverter<Dialog, JsDialog> CONVERTER = new JsEntityConverter<Dialog, JsDialog>() {
        @Override
        public JsDialog convert(Dialog src, I18nEngine formatter) {
            return JsDialog.create(
                    JsPeer.create(src.getPeer()),
                    src.getDialogTitle(), null, Placeholders.getPlaceholder(src.getPeer().getPeerId()),
                    formatter.formatShortDate(src.getDate()),
                    formatter.formatPerformerName(src.getSenderId()), false,
                    src.getText(), false,
                    Enums.convert(src.getStatus()));
        }
    };

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
