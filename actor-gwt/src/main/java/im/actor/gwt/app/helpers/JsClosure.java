package im.actor.gwt.app.helpers;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;

/**
 * Created by ex3ndr on 22.02.15.
 */
@Export
@ExportClosure
public interface JsClosure extends Exportable {
    public void callback();
}