package im.actor.gwt.app.ui;

import com.google.gwt.core.client.JsArray;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import im.actor.gwt.app.storage.JsListEngine;
import im.actor.model.entity.Dialog;

/**
 * Created by ex3ndr on 22.02.15.
 */
@ExportPackage("actor")
@Export("DialogList")
public class JsDialogList extends JsSimpleList<JsDialog, Dialog> implements Exportable {
    public JsDialogList() {
        super(null);

    }

    @Export
    public JsDialogList(JsListEngine<Dialog> engine) {
        super(engine);
    }

    @Override
    protected JsDialog convert(Dialog src) {
        return JsDialog.create(src.getPeer().getPeerId(), src.getPeer().getPeerId(), src.getDialogTitle(),
                src.getText());
    }

    public JsArray<JsDialog> values() {
        return getJsValues();
    }
}
