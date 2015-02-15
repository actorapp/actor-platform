package im.actor.model;

/**
 * Created by ex3ndr on 15.02.15.
 */
public interface OnlineCallback {
    public void onUserOnline(int uid);

    public void onUserOffline(int uid);

    public void onUserLastSeen(int uid, long lastSeen);

    public void onGroupOnline(int gid, int count);
}
