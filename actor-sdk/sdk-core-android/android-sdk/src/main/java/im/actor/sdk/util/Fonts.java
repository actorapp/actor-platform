package im.actor.sdk.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

import im.actor.runtime.android.AndroidContext;

public class Fonts {
    private static HashMap<String, Typeface> typefaces = new HashMap<String, Typeface>();

    public static Typeface load(Context context, String name) {
        if (typefaces.containsKey(name)) {
            return typefaces.get(name);
        }
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-" + name + ".ttf");
        if (typeface != null) {
            typefaces.put(name, typeface);
        }
        return typeface;
    }

    public static Typeface regular() {
        return load(AndroidContext.getContext(), "Regular");
    }

    public static Typeface italic() {
        return load(AndroidContext.getContext(), "Italic");
    }

    public static Typeface bold() {
        return load(AndroidContext.getContext(), "Bold");
    }

    public static Typeface medium() {
        return load(AndroidContext.getContext(), "Medium");
    }

    public static Typeface light() {
        return load(AndroidContext.getContext(), "Light");
    }
}
