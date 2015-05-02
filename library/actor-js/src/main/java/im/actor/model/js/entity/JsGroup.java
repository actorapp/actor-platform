/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.model.viewmodel.GroupVM;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class JsGroup extends JavaScriptObject {
    public static JsGroup fromGroupVM(GroupVM groupVM) {
        return create(groupVM.getId(), groupVM.getName().get(), groupVM.getCreatorId());
    }

    public static native JsGroup create(int uid, String title, int adminId)/*-{
        return {uid: uid, title: title, adminId: adminId};
    }-*/;

    protected JsGroup() {
        
    }
}
