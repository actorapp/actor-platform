/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import java.util.ArrayList;

import im.actor.model.PhoneBookProvider;
import im.actor.model.entity.PhoneBookContact;

public class JsPhoneBookProvider implements PhoneBookProvider {
    @Override
    public void loadPhoneBook(Callback callback) {
        callback.onLoaded(new ArrayList<PhoneBookContact>());
    }
}
