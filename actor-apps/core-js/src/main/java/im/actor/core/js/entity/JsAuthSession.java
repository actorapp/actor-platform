package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;
import im.actor.core.api.ApiAuthHolder;
import im.actor.core.api.ApiAuthSession;

public class JsAuthSession extends JavaScriptObject {
    public static JsAuthSession create(ApiAuthSession authSession) {
        String holderStr;

        if (authSession.getAuthHolder() == ApiAuthHolder.OTHERDEVICE) {
            holderStr = "OTHER_DEVICE";
        } else if (authSession.getAuthHolder() == ApiAuthHolder.THISDEVICE) {
            holderStr = "THIS_DEVICE";
        } else {
            holderStr = "UNSUPPORTED";
        }

        return create(authSession.getId(), holderStr, authSession.getAppId(), authSession.getAppTitle(),
                authSession.getDeviceTitle(), JsDate.create(authSession.getAuthTime() * 1000), authSession.getAuthLocation(),
                authSession.getLatitude(), authSession.getLongitude());
    }

    public static native JsAuthSession create(
            int id,
            String holder,
            int appId,
            String appTitle,
            String deviceTitle,
            JsDate authTime,
            String authLocation,
            Double latitude,
            Double longitude
    )/*-{
        return {
            id: id,
            holder: holder,
            appId: appId,
            appTitle: appTitle,
            deviceTitle: deviceTitle,
            authTime: authTime,
            authLocation: authLocation,
            latitude: latitude,
            longitude: longitude
        }
    }-*/;

    protected JsAuthSession() {

    }
}