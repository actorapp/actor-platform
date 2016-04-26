package im.actor.core.modules.users.router.entity;

import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterAboutChanged implements AskMessage<Void> {

    private int uid;
    private String about;

    public RouterAboutChanged(int uid, String about) {
        this.uid = uid;
        this.about = about;
    }

    public int getUid() {
        return uid;
    }

    public String getAbout() {
        return about;
    }
}
