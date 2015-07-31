package im.actor.android;

import android.content.Context;

import im.actor.android.modules.PushModule;
import im.actor.model.Configuration;

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
