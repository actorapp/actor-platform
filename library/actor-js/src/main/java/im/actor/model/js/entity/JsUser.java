package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JsUser extends JavaScriptObject {

    public static JsUser fromUserVM(UserVM userVM) {
        return create(userVM.getId(), userVM.getName().get(), userVM.isContact().get());
    }

    public static native JsUser create(int uid, String name, boolean isContact)/*-{
        return {uid: uid, name: name, isContact: isContact};
    }-*/;

    protected JsUser() {
    }
}
