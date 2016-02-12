/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers;

import java.util.ArrayList;

import im.actor.core.entity.PhoneBookContact;
import im.actor.core.providers.PhoneBookProvider;

public class JsPhoneBookProvider implements PhoneBookProvider {

    @Override
    public void loadPhoneBook(Callback callback) {
        callback.onLoaded(new ArrayList<PhoneBookContact>());
    }
}
