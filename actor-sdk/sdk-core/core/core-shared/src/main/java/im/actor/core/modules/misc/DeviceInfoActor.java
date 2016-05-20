package im.actor.core.modules.misc;

import java.util.ArrayList;

import im.actor.core.api.rpc.RequestNotifyAboutDeviceInfo;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.JavaUtil;

public class DeviceInfoActor extends ModuleActor {

    public DeviceInfoActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();

        //
        // Loading Information
        //
        ArrayList<String> langs = new ArrayList<>();
        for (String s : context().getConfiguration().getPreferredLanguages()) {
            langs.add(s);
        }
        final String timeZone = context().getConfiguration().getTimeZone();

        //
        // Checking if information changed
        //
        String expectedLangs = "";
        for (String s : langs) {
            if (!"".equals(expectedLangs)) {
                expectedLangs += ",";
            }
            expectedLangs += s.toLowerCase();
        }

        if (expectedLangs.equals(preferences().getString("device_info_langs")) &&
                JavaUtil.equalsE(timeZone, preferences().getString("device_info_timezone"))) {
            // Already sent
            return;
        }

        //
        // Performing Notification
        //
        final String finalExpectedLangs = expectedLangs;
        request(new RequestNotifyAboutDeviceInfo(langs, timeZone), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {

                // Mark as sent
                preferences().putString("device_info_langs", finalExpectedLangs);
                preferences().putString("device_info_timezone", timeZone);
            }

            @Override
            public void onError(RpcException e) {
                // Ignoring error
            }
        });
    }
}
