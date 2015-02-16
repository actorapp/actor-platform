package im.actor.model;

/**
 * Created by ex3ndr on 16.02.15.
 */
public interface MessengerCallback {
    public void onUserOnline(int uid);

    public void onUserOffline(int uid);

    public void onUserLastSeen(int uid, long lastSeen);

    public void onGroupOnline(int gid, int count);

    public void onTypingStart(int uid);

    public void onTypingEnd(int uid);

    public void onGroupTyping(int gid, int[] uids);
}
