/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.providers;

import java.util.ArrayList;

import im.actor.model.PhoneBookProvider;
import im.actor.model.entity.PhoneBookContact;

/**
 * Phone provider for environments where contact list is not available
 */
public class EmptyPhoneProvider implements PhoneBookProvider {
    @Override
    public void loadPhoneBook(Callback callback) {
        callback.onLoaded(new ArrayList<PhoneBookContact>());
    }
}
