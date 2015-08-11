/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.core.entity.Contact;
import im.actor.runtime.js.mvvm.JsEntityConverter;

public class JsContact extends JavaScriptObject {

    public static JsEntityConverter<Contact, JsContact> CONVERTER = new JsEntityConverter<Contact, JsContact>() {
        @Override
        public JsContact convert(Contact value) {

            String fileUrl = null;
//            if (value.getAvatar() != null && value.getAvatar().getSmallImage() != null) {
//                fileUrl = messenger.getFileUrl(value.getAvatar().getSmallImage().getFileReference());
//            }

            return create(value.getUid(), value.getName(),
                    Placeholders.getPlaceholder(value.getUid()), fileUrl);
        }
    };

    public static native JsContact create(int uid, String name, String placeholder, String avatar)/*-{
        return {uid: uid, name: name, placeholder: placeholder, avatar: avatar};
    }-*/;

    protected JsContact() {

    }
}
