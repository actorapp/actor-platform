package im.actor.core.modules.updates.internal;

import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUser;

public class RelatedResponse extends InternalUpdate {

    private List<ApiUser> relatedUsers;
    private List<ApiGroup> relatedGroups;
    private Runnable afterApply;

    public RelatedResponse(List<ApiUser> relatedUsers, List<ApiGroup> relatedGroups, Runnable afterApply) {
        this.relatedUsers = relatedUsers;
        this.relatedGroups = relatedGroups;
        this.afterApply = afterApply;
    }

    public List<ApiUser> getRelatedUsers() {
        return relatedUsers;
    }

    public List<ApiGroup> getRelatedGroups() {
        return relatedGroups;
    }

    public Runnable getAfterApply() {
        return afterApply;
    }
}
