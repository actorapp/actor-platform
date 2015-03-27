package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.model.Messenger;
import im.actor.model.entity.ContentType;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.js.JsMessenger;
import im.actor.model.modules.Modules;
import org.timepedia.exporter.client.Exportable;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JsDialog extends JavaScriptObject implements Exportable {

    public static final JsEntityConverter<Dialog, JsDialog> CONVERTER = new JsEntityConverter<Dialog, JsDialog>() {
        @Override
        public JsDialog convert(Dialog src, JsMessenger messenger) {

            boolean showSender = false;
            if (src.getPeer().getPeerType() == PeerType.GROUP) {
                if (src.getMessageType() != ContentType.SERVICE) {
                    showSender = true;
                }
            }
            String senderName = null;
            if (showSender) {
                senderName = messenger.getUsers().get(src.getSenderId()).getName().get();
            }

            String date = messenger.getFormatter().formatShortDate(src.getDate());

            boolean highlightContent = src.getMessageType() != ContentType.TEXT;
            String messageText = messenger.getFormatter().formatContentDialogText(src.getSenderId(),
                    src.getMessageType(), src.getText(), src.getRelatedUid());

            return JsDialog.create(
                    JsPeerInfo.create(JsPeer.create(src.getPeer()), src.getDialogTitle(), null,
                            Placeholders.getPlaceholder(src.getPeer().getPeerId())),
                    // Message
                    date,
                    senderName, showSender,
                    messageText, highlightContent,
                    Enums.convert(src.getStatus()));
        }
    };

    public static native JsDialog create(JsPeerInfo peer,
                                         String date,
                                         String sender, boolean showSender,
                                         String text, boolean isHighlighted,
                                         String state)/*-{
        return {peer: peer, text: text, date: date, sender: sender, showSender: showSender,
        isHighlighted: isHighlighted, state:state };
    }-*/;

    protected JsDialog() {
    }
}
