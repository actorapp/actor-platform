package im.actor.core.js.modules;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;

@Export
@ExportClosure
public interface JsIdleCallback {
    void onActionDetected();
}
