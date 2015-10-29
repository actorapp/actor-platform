package im.actor.core.modules.internal;

import java.util.ArrayList;

import im.actor.core.api.rpc.RequestNotifyAboutDeviceInfo;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;

public class DeviceInfoModule extends AbsModule {

    public DeviceInfoModule(ModuleContext context) {
        super(context);
    }

    public void run() {
        ArrayList<String> langs = new ArrayList<String>();
        for (String s : context().getConfiguration().getPreferredLanguages()) {
            langs.add(s);
        }
        String timeZone = context().getConfiguration().getTimeZone();
        request(new RequestNotifyAboutDeviceInfo(langs, timeZone));
    }
}
