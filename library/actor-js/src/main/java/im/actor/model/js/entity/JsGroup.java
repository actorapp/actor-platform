/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.HashSet;

import im.actor.model.entity.Avatar;
import im.actor.model.entity.GroupMember;
import im.actor.model.entity.Peer;
import im.actor.model.js.JsMessenger;
import im.actor.model.viewmodel.GroupVM;

public class JsGroup extends JavaScriptObject {
    public static JsGroup fromGroupVM(GroupVM groupVM, JsMessenger messenger) {
        int online = groupVM.getPresence().get();
        String presence = messenger.getFormatter().formatGroupMembers(groupVM.getMembersCount());
        if (online > 0) {
            presence += ", " + messenger.getFormatter().formatGroupOnline(online);
        }
        String fileUrl = null;
        String bigFileUrl = null;
        Avatar avatar = groupVM.getAvatar().get();
        if (avatar != null) {
            if (avatar.getSmallImage() != null) {
                fileUrl = messenger.getFileUrl(avatar.getSmallImage().getFileReference());
            }
            if (avatar.getLargeImage() != null) {
                bigFileUrl = messenger.getFileUrl(avatar.getLargeImage().getFileReference());
            }
        }

        JsArray<JsGroupMember> convertedMembers = JsArray.createArray().cast();
        HashSet<GroupMember> groupMembers = groupVM.getMembers().get();
        GroupMember[] members = groupMembers.toArray(new GroupMember[groupMembers.size()]);
        for (GroupMember g : members) {
            JsPeerInfo peerInfo = messenger.buildPeerInfo(Peer.user(g.getUid()));
            convertedMembers.push(JsGroupMember.create(peerInfo,
                    g.isAdministrator()));
        }

        return create(groupVM.getId(), groupVM.getName().get(), fileUrl, bigFileUrl,
                Placeholders.getPlaceholder(groupVM.getId()), groupVM.getCreatorId(), presence,
                convertedMembers);
    }

    public static native JsGroup create(int id, String name, String avatar, String bigAvatar,
                                        String placeholder, int adminId, String presence,
                                        JsArray<JsGroupMember> members)/*-{
        return {id: id, name: name, avatar: avatar, bigAvatar: bigAvatar, placeholder:placeholder,
            adminId: adminId, presence: presence, members: members};
    }-*/;

    protected JsGroup() {

    }

    public native final int getGid()/*-{
        return this.id;
    }-*/;
}
