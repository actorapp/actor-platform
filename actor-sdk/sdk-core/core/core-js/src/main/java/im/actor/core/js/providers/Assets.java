package im.actor.core.js.providers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.TextResource;

public interface Assets extends ClientBundleWithLookup {

    Assets INSTANCE = GWT.create(Assets.class);

    @Source("AppText.properties")
    TextResource AppText_properties();

    @Source("AppText_Ar.properties")
    TextResource AppText_Ar_properties();

    @Source("AppText_Cn.properties")
    TextResource AppText_Cn_properties();

    @Source("AppText_Es.properties")
    TextResource AppText_Es_properties();

    @Source("AppText_Pt.properties")
    TextResource AppText_Pt_properties();

    @Source("AppText_Ru.properties")
    TextResource AppText_Ru_properties();

    @Source("Months.properties")
    TextResource Months_properties();

    @Source("Months_Ar.properties")
    TextResource Months_Ar_properties();

    @Source("Months_Cn.properties")
    TextResource Months_Cn_properties();

    @Source("Months_Es.properties")
    TextResource Months_Es_properties();

    @Source("Months_Pt.properties")
    TextResource Months_Pt_properties();

    @Source("Months_Ru.properties")
    TextResource Months_Ru_properties();
}
