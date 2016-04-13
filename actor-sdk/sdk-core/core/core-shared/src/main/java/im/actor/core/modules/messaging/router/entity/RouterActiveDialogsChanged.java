package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.api.ApiDialogGroup;

public class RouterActiveDialogsChanged implements RouterMessageOnlyActive {

    private List<ApiDialogGroup> groups;
    private boolean showInvite;
    private boolean hasArchived;

    public RouterActiveDialogsChanged(List<ApiDialogGroup> groups, boolean showInvite, boolean hasArchived) {
        this.groups = groups;
        this.showInvite = showInvite;
        this.hasArchived = hasArchived;
    }

    public List<ApiDialogGroup> getGroups() {
        return groups;
    }

    public boolean isShowInvite() {
        return showInvite;
    }

    public boolean isHasArchived() {
        return hasArchived;
    }
}
