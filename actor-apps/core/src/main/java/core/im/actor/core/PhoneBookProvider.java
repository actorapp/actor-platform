/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

import im.actor.core.entity.PhoneBookContact;

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
    @ObjectiveCName("loadPhoneBookWithCallback:")
    void loadPhoneBook(Callback callback);

    /**
     * Callback about phone book load
     */
    interface Callback {
        /**
         * On PhoneBook loaded
         *
         * @param contacts loaded contacts
         */
        @ObjectiveCName("onLoadedWithContacts:")
        void onLoaded(List<PhoneBookContact> contacts);
    }
}
