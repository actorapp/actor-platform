package im.actor.model.providers;

import java.util.ArrayList;

import im.actor.model.PhoneBookProvider;
import im.actor.model.entity.PhoneBookContact;

/**
 * Created by ex3ndr on 01.04.15.
 */
public class EmptyPhoneProvider implements PhoneBookProvider {
    @Override
    public void loadPhoneBook(Callback callback) {
        callback.onLoaded(new ArrayList<PhoneBookContact>());
    }
}
