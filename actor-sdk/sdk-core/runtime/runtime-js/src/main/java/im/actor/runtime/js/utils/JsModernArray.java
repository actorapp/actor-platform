package im.actor.runtime.js.utils;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsModernArray<T extends JavaScriptObject> extends JsArray<T> {

    public final native void clear()/*-{ this.splice(0, this.length); }-*/;

    public final native void insert(int index, T obj)/*-{ this.splice(index, 0, obj); }-*/;

    public final native void remove(int index)/*-{ this.splice(index, 1); }-*/;

    public final void update(int index, T obj) {
        remove(index);
        insert(index, obj);
    }

    public final native JsArray<T> reverse()/*-{ return this.slice().reverse(); }-*/;

    protected JsModernArray() {

    }
}
