package im.actor.gwt.app.ui;

import im.actor.gwt.app.helpers.Enums;
import im.actor.gwt.app.helpers.Placeholders;
import im.actor.model.Messenger;
import im.actor.model.entity.Dialog;
import im.actor.model.i18n.I18nEngine;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JsDialogEntityConverter implements JsEntityConverter<Dialog, JsDialog> {
    private Messenger messenger;
    private I18nEngine engine;

    public JsDialogEntityConverter(Messenger messenger) {
        this.messenger = messenger;
        this.engine = messenger.getFormatter();
    }

    @Override
    public JsDialog convert(Dialog src) {
        return JsDialog.create(
                JsPeer.create(src.getPeer()),
                src.getDialogTitle(), null, Placeholders.getPlaceholder(src.getPeer().getPeerId()),
                engine.formatShortDate(src.getDate()),
                "<SENDER>", false,
                src.getText(), false,
                Enums.convert(src.getStatus()));
    }
}
