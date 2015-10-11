/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers;

import java.util.ArrayList;

import im.actor.core.PhoneBookProvider;
import im.actor.core.entity.PhoneBookContact;

public class JsPhoneBookProvider implements PhoneBookProvider {
    @Override
    public void loadPhoneBook(Callback callback) {
        callback.onLoaded(new ArrayList<PhoneBookContact>());
    }
}
