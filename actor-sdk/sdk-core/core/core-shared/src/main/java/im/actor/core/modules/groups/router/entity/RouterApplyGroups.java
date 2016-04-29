package im.actor.core.modules.groups.router.entity;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class RouterApplyGroups implements AskMessage<Void> {
    
    private List<ApiGroup> groups;

    public RouterApplyGroups(List<ApiGroup> groups) {
        this.groups = groups;
    }

    public List<ApiGroup> getGroups() {
        return groups;
    }
}
