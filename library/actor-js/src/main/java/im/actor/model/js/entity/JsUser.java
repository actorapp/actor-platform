/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.model.entity.Avatar;
import im.actor.model.js.JsMessenger;
import im.actor.model.viewmodel.UserPresence;
import im.actor.model.viewmodel.UserVM;

public class JsUser extends JavaScriptObject {

    public static JsUser fromUserVM(UserVM userVM, JsMessenger messenger) {
        UserPresence presence = userVM.getPresence().get();
        String presenceString = messenger.getFormatter().formatPresence(presence, userVM.getSex());
        String fileUrl = null;
        Avatar avatar = userVM.getAvatar().get();
        if (avatar != null && avatar.getSmallImage() != null) {
            fileUrl = messenger.getFileUrl(avatar.getSmallImage().getFileReference());
        }
        return create(userVM.getId(), userVM.getName().get(),
                fileUrl,
                Placeholders.getPlaceholder(userVM.getId()),
                userVM.isContact().get(),
                presenceString);
    }

    public static native JsUser create(int id, String name, String avatar, String placeholder, boolean isContact, String presence)/*-{
        return {id: id, name: name, avatar: avatar, placeholder: placeholder, isContact: isContact, presence: presence};
    }-*/;

    protected JsUser() {
    }

    public native final int getUid()/*-{
        return this.id;
    }-*/;
}
