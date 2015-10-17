package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.Peer;

public class DialogSmall {

    @Property("readonly, nonatomic")
    private final Peer peer;
    @Property("readonly, nonatomic")
    private final String title;
    @Property("readonly, nonatomic")
    private final Avatar avatar;
    @Property("readonly, nonatomic")
    private final int counter;

    public DialogSmall(Peer peer, String title, Avatar avatar, int counter) {
        this.peer = peer;
        this.title = title;
        this.avatar = avatar;
        this.counter = counter;
    }

    public Peer getPeer() {
        return peer;
    }

    public String getTitle() {
        return title;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public int getCounter() {
        return counter;
    }
}
