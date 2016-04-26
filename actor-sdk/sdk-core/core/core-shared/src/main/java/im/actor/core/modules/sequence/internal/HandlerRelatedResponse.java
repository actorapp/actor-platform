package im.actor.core.modules.sequence.internal;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUser;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class HandlerRelatedResponse implements AskMessage<Void> {

    private List<ApiUser> relatedUsers;
    private List<ApiGroup> relatedGroups;

    public HandlerRelatedResponse(List<ApiUser> relatedUsers, List<ApiGroup> relatedGroups) {
        this.relatedUsers = relatedUsers;
        this.relatedGroups = relatedGroups;
    }

    public List<ApiUser> getRelatedUsers() {
        return relatedUsers;
    }

    public List<ApiGroup> getRelatedGroups() {
        return relatedGroups;
    }
}
