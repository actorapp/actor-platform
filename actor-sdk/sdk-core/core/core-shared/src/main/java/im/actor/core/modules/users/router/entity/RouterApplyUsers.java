package im.actor.core.modules.users.router.entity;

import java.util.List;

import im.actor.core.api.ApiUser;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterApplyUsers implements AskMessage<Void> {

    private List<ApiUser> users;

    public RouterApplyUsers(List<ApiUser> users) {
        this.users = users;
    }

    public List<ApiUser> getUsers() {
        return users;
    }
}
