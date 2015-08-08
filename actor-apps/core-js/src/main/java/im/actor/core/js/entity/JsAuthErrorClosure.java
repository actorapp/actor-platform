/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;

@Export
@ExportClosure
public interface JsAuthErrorClosure extends Exportable {
    void onError(String tag, String message, boolean canTryAgain, String newState);
}