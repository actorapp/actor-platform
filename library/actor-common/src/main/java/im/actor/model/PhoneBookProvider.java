/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import java.util.List;

import im.actor.model.entity.PhoneBookContact;

/**
 * Provider for PhoneBook import. If not available use EmptyPhoneProvider.
 * Call Messenger.onPhoneBookChanged when you detect possible phone book changes.
 */
public interface PhoneBookProvider {
    /**
     * Perform async phone book loading
     *
     * @param callback completion callback
     */
    public void loadPhoneBook(Callback callback);

    /**
     * Callback about phone book load
     */
    public interface Callback {
        /**
         * On PhoneBook loaded
         *
         * @param contacts loaded contacts
         */
        public void onLoaded(List<PhoneBookContact> contacts);
    }
}
