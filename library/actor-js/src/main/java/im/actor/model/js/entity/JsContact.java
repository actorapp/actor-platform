/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.model.entity.Contact;
import im.actor.model.js.JsMessenger;

public class JsContact extends JavaScriptObject {

    public static JsEntityConverter<Contact, JsContact> CONVERTER = new JsEntityConverter<Contact, JsContact>() {
        @Override
        public JsContact convert(Contact value, JsMessenger messenger) {

            String fileUrl = null;
            if (value.getAvatar() != null && value.getAvatar().getSmallImage() != null) {
                fileUrl = messenger.getFileUrl(value.getAvatar().getSmallImage().getFileReference());
            }

            return create(value.getUid(), value.getName(),
                    Placeholders.getPlaceholder(value.getUid()), fileUrl);
        }
    };

    public static native JsContact create(int uid, String name, String placeholder, String avatar)/*-{

    }-*/;

    protected JsContact() {

    }
}
