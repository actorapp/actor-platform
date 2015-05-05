/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.locale;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface LocaleBundle extends ClientBundle {
    @Source("AppText.properties")
    TextResource AppText();

    @Source("Months.properties")
    TextResource Months();
}
