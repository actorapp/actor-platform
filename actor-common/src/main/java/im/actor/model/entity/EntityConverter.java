package im.actor.model.entity;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class EntityConverter {
    public static User convert(im.actor.model.api.User user) {
        return new User(user.getId(), user.getAccessHash(), user.getName(), user.getLocalName());
    }
}
