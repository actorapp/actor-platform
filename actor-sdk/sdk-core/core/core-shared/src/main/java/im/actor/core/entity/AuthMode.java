package im.actor.core.entity;

import im.actor.core.api.ApiEmailActivationType;
import im.actor.core.api.ApiPhoneActivationType;

public enum AuthMode {
    OTP, PASSWORD, OAUTH2, UNSUPPORTED;

    public static AuthMode fromApi(ApiEmailActivationType activationType) {
        switch (activationType) {
            case CODE:
                return OTP;
            case PASSWORD:
                return PASSWORD;
            case OAUTH2:
                return OAUTH2;
            default:
            case UNSUPPORTED_VALUE:
                return UNSUPPORTED;
        }
    }

    public static AuthMode fromApi(ApiPhoneActivationType activationType) {
        switch (activationType) {
            case CODE:
                return OTP;
            case PASSWORD:
                return PASSWORD;
            default:
            case UNSUPPORTED_VALUE:
                return UNSUPPORTED;
        }
    }
}
