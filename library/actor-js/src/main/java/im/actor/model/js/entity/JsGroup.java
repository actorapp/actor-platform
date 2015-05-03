/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.model.entity.Avatar;
import im.actor.model.js.JsMessenger;
import im.actor.model.viewmodel.GroupVM;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class JsGroup extends JavaScriptObject {
    public static JsGroup fromGroupVM(GroupVM groupVM, JsMessenger messenger) {
        int online = groupVM.getPresence().get();
        String presence = messenger.getFormatter().formatGroupMembers(groupVM.getMembersCount());
        if (online > 0) {
            presence = ", " + messenger.getFormatter().formatGroupOnline(online);
        }
        String fileUrl = null;
        Avatar avatar = groupVM.getAvatar().get();
        if (avatar != null && avatar.getSmallImage() != null) {
            fileUrl = messenger.getFileUrl(avatar.getSmallImage().getFileReference());
        }
        return create(groupVM.getId(), groupVM.getName().get(), fileUrl, Placeholders.getPlaceholder(groupVM.getId()), groupVM.getCreatorId(), presence);
    }

    public static native JsGroup create(int gid, String title, String avatar, String placeholder, int adminId, String presence)/*-{
        return {id: id, name: name, adminId: adminId, presence: presence};
    }-*/;

    protected JsGroup() {

    }

    public native final int getGid()/*-{
        return this.id;
    }-*/;
}
