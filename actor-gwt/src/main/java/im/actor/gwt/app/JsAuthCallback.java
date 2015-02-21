package im.actor.gwt.app;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

/**
 * Created by ex3ndr on 21.02.15.
 */
@Export
public interface JsAuthCallback extends Exportable {
    public void onResult(JsAuthState newState);

    public void onError(String tag, String message, boolean canTryAgain, JsAuthState newState);
}