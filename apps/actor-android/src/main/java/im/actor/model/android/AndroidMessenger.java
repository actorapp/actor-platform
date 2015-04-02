package im.actor.model.android;

import android.content.Context;

import im.actor.android.AndroidBaseMessenger;
import im.actor.model.Configuration;
import im.actor.model.android.modules.PushModule;

/**
 * Created by ex3ndr on 02.04.15.
 */
public class AndroidMessenger extends AndroidBaseMessenger {

    private PushModule pushModule;

    public AndroidMessenger(Context context, Configuration configuration) {
        super(context, configuration);

        pushModule = new PushModule(context, modules);
    }
}
