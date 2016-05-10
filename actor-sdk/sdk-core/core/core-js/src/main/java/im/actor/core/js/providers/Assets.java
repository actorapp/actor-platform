package im.actor.core.js.providers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.TextResource;

public interface Assets extends ClientBundleWithLookup {

    Assets INSTANCE = GWT.create(Assets.class);

    @Source("AppText.json")
    TextResource AppText_json();

    @Source("AppText_Ar.json")
    TextResource AppText_Ar_json();

    @Source("AppText_Zn.json")
    TextResource AppText_Zn_json();

    @Source("AppText_Es.json")
    TextResource AppText_Es_json();

    @Source("AppText_Pt.json")
    TextResource AppText_Pt_json();

    @Source("AppText_Ru.json")
    TextResource AppText_Ru_json();
}
