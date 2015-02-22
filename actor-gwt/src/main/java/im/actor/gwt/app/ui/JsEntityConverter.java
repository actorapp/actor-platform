package im.actor.gwt.app.ui;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by ex3ndr on 22.02.15.
 */
public interface JsEntityConverter<F, T extends JavaScriptObject> {
    public T convert(F value);
}
