package im.actor.messenger.app.view;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

import im.actor.messenger.app.AppContext;

/**
 * Created by ex3ndr on 01.09.14.
 */
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
        return load(AppContext.getContext(), "Regular");
    }

    public static Typeface italic() {
        return load(AppContext.getContext(), "Italic");
    }

    public static Typeface bold() {
        return load(AppContext.getContext(), "Bold");
    }

    public static Typeface medium() {
        return load(AppContext.getContext(), "Medium");
    }

    public static Typeface light() {
        return load(AppContext.getContext(), "Light");
    }
}
