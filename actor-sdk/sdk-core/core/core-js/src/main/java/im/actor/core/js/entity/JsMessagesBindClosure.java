package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;

@Export
@ExportClosure
public interface JsMessagesBindClosure extends Exportable {
    void onBind(JsArray<JsMessage> array,
                JsArray<JavaScriptObject> overlays,
                boolean isLoaded,
                double receiveDate,
                double readDate,
                double readByMeDate);
}
