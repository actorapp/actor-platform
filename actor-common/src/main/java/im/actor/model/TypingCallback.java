package im.actor.model;

/**
 * Created by ex3ndr on 16.02.15.
 */
public interface TypingCallback {
    public void onTypingStart(int uid);

    public void onTypingEnd(int uid);

    public void onGroupTyping(int gid, int[] uids);
}