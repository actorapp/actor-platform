package im.actor.core.modules.users.router.entity;

import im.actor.core.api.ApiAvatar;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterAvatarChanged implements AskMessage<Void> {

    private int uid;
    private ApiAvatar avatar;

    public RouterAvatarChanged(int uid, ApiAvatar avatar) {
        this.uid = uid;
        this.avatar = avatar;
    }

    public int getUid() {
        return uid;
    }

    public ApiAvatar getAvatar() {
        return avatar;
    }
}
