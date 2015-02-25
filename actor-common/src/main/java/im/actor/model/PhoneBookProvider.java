package im.actor.model;

import java.util.List;

import im.actor.model.entity.PhoneBookContact;

/**
 * Created by ex3ndr on 25.02.15.
 */
public interface PhoneBookProvider {
    public void loadPhoneBook(Callback callback);

    public interface Callback {
        public void onLoaded(List<PhoneBookContact> contacts);
    }
}
