package im.actor.core.entity;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.core.api.ApiAdminSettings;
import im.actor.runtime.collections.SparseArray;

public class GroupPermissions {

    private ApiAdminSettings settings;

    public GroupPermissions(ApiAdminSettings settings) {
        this.settings = settings;
    }

    @ObjectiveCName("isMembersCanEditInfo")
    public boolean isMembersCanEditInfo() {
        return settings.canMembersEditGroupInfo();
    }

    @ObjectiveCName("setMembersCanEditInfo:")
    public void setMembersCanEditInfo(boolean canEditInfo) {
        SparseArray<Object> unmapped = settings.getUnmappedObjects();
        settings = new ApiAdminSettings(
                settings.showAdminsToMembers(),
                settings.canMembersInvite(),
                canEditInfo,
                settings.canAdminsEditGroupInfo(),
                settings.showJoinLeaveMessages()
        );
        settings.setUnmappedObjects(unmapped);
    }

    @ObjectiveCName("isShowJoinLeaveMessages")
    public boolean isShowJoinLeaveMessages() {
        return settings.showJoinLeaveMessages();
    }

    @ObjectiveCName("setShowJoinLeaveMessages:")
    public void setShowJoinLeaveMessages(boolean showJoinLeaveMessages) {
        SparseArray<Object> unmapped = settings.getUnmappedObjects();
        settings = new ApiAdminSettings(
                settings.showAdminsToMembers(),
                settings.canMembersInvite(),
                settings.canMembersEditGroupInfo(),
                settings.canAdminsEditGroupInfo(),
                showJoinLeaveMessages
        );
        settings.setUnmappedObjects(unmapped);
    }

    public ApiAdminSettings getApiSettings() {
        return settings;
    }
}
