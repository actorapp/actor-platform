package im.actor.gwt.app.ui;

import com.google.gwt.core.client.JsArray;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import im.actor.gwt.app.helpers.Enums;
import im.actor.gwt.app.helpers.Placeholders;
import im.actor.gwt.app.storage.JsListEngine;
import im.actor.model.entity.Dialog;

/**
 * Created by ex3ndr on 22.02.15.
 */
@ExportPackage("actor")
@Export("DialogList")
public class JsDialogList extends JsBaseList<JsDialog, Dialog> implements Exportable {
    public JsDialogList() {
        super(null);

    }

    @Export
    public JsDialogList(JsListEngine<Dialog> engine) {
        super(engine);
    }

    @Override
    protected JsDialog convert(Dialog src) {
        return JsDialog.create(
                JsPeer.create(src.getPeer()),
                src.getDialogTitle(), null, Placeholders.getPlaceholder(src.getPeer().getPeerId()),
                (int) (src.getDate() / 1000L),
                "<SENDER>", false,
                src.getText(), false,
                Enums.convert(src.getStatus()));
    }

    public JsArray<JsDialog> values() {
        return getJsValues();
    }
}
