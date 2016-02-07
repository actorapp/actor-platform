package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;

@Export
@ExportClosure
public interface JsEventBusCallback extends Exportable {
    void onEvent(String tag, JavaScriptObject item);
}