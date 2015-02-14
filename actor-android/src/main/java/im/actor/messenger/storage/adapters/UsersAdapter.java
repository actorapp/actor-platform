package im.actor.messenger.storage.adapters;

import com.droidkit.engine.keyvalue.DataAdapter;
import im.actor.model.entity.User;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class UsersAdapter implements DataAdapter<User> {

    @Override
    public long getId(User value) {
        return value.getEngineId();
    }

    @Override
    public byte[] serialize(User entity) {
        return entity.toByteArray();
    }

    @Override
    public User deserialize(byte[] item) {
        try {
            return User.fromBytes(item);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
