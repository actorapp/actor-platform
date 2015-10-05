/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import im.actor.core.entity.Avatar;
import im.actor.core.js.JsMessenger;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserPresence;
import im.actor.core.viewmodel.UserVM;
import im.actor.core.viewmodel.generics.ArrayListUserPhone;

public class JsUser extends JavaScriptObject {

    public static JsUser fromUserVM(UserVM userVM, JsMessenger messenger) {
        UserPresence presence = userVM.getPresence().get();
        String presenceString = messenger.getFormatter().formatPresence(presence, userVM.getSex());
        String fileUrl = null;
        String bigFileUrl = null;
        Avatar avatar = userVM.getAvatar().get();
        if (avatar != null) {
            if (avatar.getSmallImage() != null) {
                fileUrl = messenger.getFileUrl(avatar.getSmallImage().getFileReference());
            }
            if (avatar.getLargeImage() != null) {
                bigFileUrl = messenger.getFileUrl(avatar.getLargeImage().getFileReference());
            }
        }
        JsArray<JsPhone> convertedPhones = JsArray.createArray().cast();
        ArrayListUserPhone phones = userVM.getPhones().get();
        for (UserPhone p : phones) {
            convertedPhones.push(JsPhone.create(p.getPhone() + "", p.getTitle()));
        }

        return create(userVM.getId(), userVM.getName().get(), userVM.getNick().get(),
                userVM.getAbout().get(),
                fileUrl, bigFileUrl,
                Placeholders.getPlaceholder(userVM.getId()),
                userVM.isContact().get(), userVM.isBot(),
                presenceString, convertedPhones);
    }

    public static native JsUser create(int id, String name, String nick, String about,
                                       String avatar, String bigAvatar, String placeholder,
                                       boolean isContact, boolean isBot, String presence, JsArray<JsPhone> phones)/*-{
        return {id: id, name: name, nick: nick, about: about, avatar: avatar, bigAvatar: bigAvatar, placeholder: placeholder,
            isContact: isContact, presence: presence, phones: phones};
    }-*/;

    protected JsUser() {
    }

    public native final int getUid()/*-{
        return this.id;
    }-*/;
}
