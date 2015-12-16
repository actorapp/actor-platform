package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.core.api.ApiColor;
import im.actor.core.api.ApiPredefinedColor;
import im.actor.core.api.ApiRgbColor;

public class JsColor extends JavaScriptObject {

    public static JsColor create(ApiColor color) {
        if (color == null) {
            return null;
        }
        if (color instanceof ApiPredefinedColor) {
            switch (((ApiPredefinedColor) color).getColor()) {
                case GREEN:
                    return createNamed("green");
                case RED:
                    return createNamed("red");
                case YELLOW:
                    return createNamed("yellow");
                case UNSUPPORTED_VALUE:
                    return null;
            }
        } else if (color instanceof ApiRgbColor) {
            String r = intTo2BytesStr((((ApiRgbColor) color).getRgb() & 0xFF0000) >> 16);
            String g = intTo2BytesStr((((ApiRgbColor) color).getRgb() & 0xFF00) >> 8);
            String b = intTo2BytesStr((((ApiRgbColor) color).getRgb() & 0xFF));
            return createHex("#" + r + g + b);
        }
        return null;
    }

    public static native JsColor createHex(String hex)/*-{
        return { type: "hex", hex: hex };
    }-*/;

    public static native JsColor createNamed(String name)/*-{
        return { type: "named", name: name };
    }-*/;

    protected JsColor() {

    }

    private static String intTo2BytesStr(int i) {
        return pad(Integer.toHexString(intTo2Bytes(i)));
    }

    private static int intTo2Bytes(int i) {
        return (i < 0) ? 0 : (i > 255) ? 255 : i;
    }

    private static String pad(String str) {
        StringBuilder sb = new StringBuilder(str);
        if (sb.length() < 2) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

}
