package im.actor.model.js.providers.locale;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * Created by ex3ndr on 22.02.15.
 */
public interface LocaleBundle extends ClientBundle {
    @Source("AppText.properties")
    public TextResource AppText();

    @Source("Months.properties")
    public TextResource Months();
}
