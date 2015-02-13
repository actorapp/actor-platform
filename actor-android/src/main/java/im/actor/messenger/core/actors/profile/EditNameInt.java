package im.actor.messenger.core.actors.profile;

import com.droidkit.actors.concurrency.Future;

/**
 * Created by ex3ndr on 25.10.14.
 */
public interface EditNameInt {
    public Future<Boolean> editMyName(String newName);

    public Future<Boolean> editName(int uid, String newName);
}
