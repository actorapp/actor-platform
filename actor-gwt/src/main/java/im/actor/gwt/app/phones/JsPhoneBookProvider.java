package im.actor.gwt.app.phones;

import java.util.ArrayList;

import im.actor.model.PhoneBookProvider;
import im.actor.model.entity.PhoneBookContact;

/**
 * Created by ex3ndr on 28.02.15.
 */
public class JsPhoneBookProvider implements PhoneBookProvider {
    @Override
    public void loadPhoneBook(Callback callback) {
        callback.onLoaded(new ArrayList<PhoneBookContact>());
    }
}
