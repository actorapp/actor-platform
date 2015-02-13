package im.actor.messenger.core.actors.profile;

/**
 * Created by ex3ndr on 17.09.14.
 */
public interface AvatarChangeInt {
    public void changeAvatar(String fileName);

    public void clearAvatar();

    public void cancelChangingAvatar();

    public void tryAgain();
}
