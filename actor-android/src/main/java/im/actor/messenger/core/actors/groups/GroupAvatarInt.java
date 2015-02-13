package im.actor.messenger.core.actors.groups;

/**
 * Created by ex3ndr on 30.11.14.
 */
public interface GroupAvatarInt {
    public void changeAvatar(int gid, String fileName);

    public void clearAvatar(int gid);

    public void cancelChangingAvatar(int gid);

    public void tryAgain(int gid);
}
