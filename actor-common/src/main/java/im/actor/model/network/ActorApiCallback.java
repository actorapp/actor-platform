package im.actor.model.network;

import im.actor.model.api.ContactRecord;
import im.actor.model.api.Group;
import im.actor.model.api.User;
import im.actor.model.network.parser.Update;

import java.util.List;

/**
 * Created by ex3ndr on 08.02.15.
 */
public interface ActorApiCallback {
    public void onAuthIdInvalidated(long authKey);

    public void onNewSessionCreated();

    public void onSeqFatUpdate(int seq, byte[] state, Update update,
                               List<User> users, List<Group> groups, List<ContactRecord> contactRecords);

    public void onSeqUpdate(int seq, byte[] state, Update update);

    public void onSeqTooLong();

    public void onWeakUpdate(long date, Update update);
}