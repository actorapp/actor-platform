package im.actor.core.js.entity;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;

@Export
@ExportClosure
public interface JsLogCallback {
    void log(String tag, String level, String message);
}
