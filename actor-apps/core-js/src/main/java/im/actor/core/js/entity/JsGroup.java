/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import im.actor.core.entity.Avatar;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.js.JsMessenger;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

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

        ArrayList<JsGroupMember> convertedMembers = new ArrayList<JsGroupMember>();
        HashSet<GroupMember> groupMembers = groupVM.getMembers().get();
        GroupMember[] members = groupMembers.toArray(new GroupMember[groupMembers.size()]);
        for (GroupMember g : members) {
            JsPeerInfo peerInfo = messenger.buildPeerInfo(Peer.user(g.getUid()));
            Log.d("JsGroup", "PeerInfo: " + peerInfo);
            convertedMembers.add(JsGroupMember.create(peerInfo,
                    g.isAdministrator(),
                    g.getInviterUid() == messenger.myUid() || groupVM.getCreatorId() == messenger.myUid()));
        }
        Collections.sort(convertedMembers, new Comparator<JsGroupMember>() {
            @Override
            public int compare(JsGroupMember o1, JsGroupMember o2) {
                return o1.getPeerInfo().getTitle().compareToIgnoreCase(o2.getPeerInfo().getTitle());
            }
        });
        JsArray<JsGroupMember> jsMembers = JsArray.createArray().cast();
        for (JsGroupMember member : convertedMembers) {
            jsMembers.push(member);
        }
        return create(groupVM.getId(), groupVM.getName().get(), groupVM.getAbout().get(), fileUrl, bigFileUrl,
                Placeholders.getPlaceholder(groupVM.getId()), groupVM.getCreatorId(), presence,
                jsMembers);
    }

    public static native JsGroup create(int id, String name, String about, String avatar, String bigAvatar,
                                        String placeholder, int adminId, String presence,
                                        JsArray<JsGroupMember> members)/*-{
        return {
            id: id, name: name, about: about, avatar: avatar, bigAvatar: bigAvatar, placeholder: placeholder,
            adminId: adminId, presence: presence, members: members
        };
    }-*/;

    protected JsGroup() {

    }

    public native final int getGid()/*-{
        return this.id;
    }-*/;
}
