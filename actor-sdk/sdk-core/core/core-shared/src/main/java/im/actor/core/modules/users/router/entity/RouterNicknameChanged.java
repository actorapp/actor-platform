package im.actor.core.modules.users.router.entity;

import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterNicknameChanged implements AskMessage<Void> {

    private int uid;
    private String nickname;

    public RouterNicknameChanged(int uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
    }

    public int getUid() {
        return uid;
    }

    public String getNickname() {
        return nickname;
    }
}
