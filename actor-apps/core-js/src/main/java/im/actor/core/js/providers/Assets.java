package im.actor.core.js.providers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.TextResource;

public interface Assets extends ClientBundleWithLookup {

    Assets INSTANCE = GWT.create(Assets.class);

    @Source("AppText.properties")
    TextResource AppText_properties();

    @Source("Months.properties")
    TextResource Months_properties();
}
