package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.entity.Group;
import im.actor.core.entity.User;

public class RouterPeersChanged {

    private List<User> users;
    private List<Group> groups;

    public RouterPeersChanged(List<User> users, List<Group> groups) {
        this.users = users;
        this.groups = groups;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Group> getGroups() {
        return groups;
    }
}
