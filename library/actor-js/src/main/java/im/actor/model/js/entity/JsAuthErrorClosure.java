/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;

/**
 * Created by ex3ndr on 22.02.15.
 */
@Export
@ExportClosure
public interface JsAuthErrorClosure extends Exportable {
    public void onError(String tag, String message, boolean canTryAgain, String newState);
}