/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.angular;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;

/**
 * Created by ex3ndr on 27.03.15.
 */
@Export
@ExportClosure
public interface AngularValueCallback extends Exportable {
    public void onChanged(Object obj);
}
