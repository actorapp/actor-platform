/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js;

import im.actor.model.Configuration;
import im.actor.model.Messenger;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.js.angular.AngularFilesModule;
import im.actor.model.js.angular.AngularList;
import im.actor.model.js.angular.AngularModule;
import im.actor.model.js.angular.AngularValue;
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.entity.JsGroup;
import im.actor.model.js.entity.JsMessage;
import im.actor.model.js.entity.JsPeer;
import im.actor.model.js.entity.JsPeerInfo;
import im.actor.model.js.entity.JsTyping;
import im.actor.model.js.entity.JsUser;
import im.actor.model.js.entity.Placeholders;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

public class JsMessenger extends Messenger {

    private AngularModule angularModule;
    private AngularFilesModule angularFilesModule;

    public JsMessenger(Configuration configuration) {
        super(configuration);
        angularFilesModule = new AngularFilesModule(modules);
        angularModule = new AngularModule(this, angularFilesModule, modules);
    }

    public void onMessageShown(Peer peer, Long sortKey) {
        modules.getMessagesModule().onInMessageShown(peer, sortKey);
    }

    public void loadMoreDialogs() {
        modules.getMessagesModule().loadMoreDialogs();
    }

    public void loadMoreHistory(Peer peer) {
        modules.getMessagesModule().loadMoreHistory(peer);
    }

    public AngularList<JsDialog, Dialog> getDialogsList() {
        return angularModule.getDialogsList();
    }

    public AngularList<JsMessage, Message> getConversationList(Peer peer) {
        return angularModule.getMessagesList(peer);
    }

    public AngularValue<JsUser> getUser(int uid) {
        return angularModule.getUser(uid);
    }

    public AngularValue<JsGroup> getGroup(int gid) {
        return angularModule.getGroup(gid);
    }

    public AngularValue<JsTyping> getTyping(Peer peer) {
        return angularModule.getTyping(peer);
    }

    public JsPeerInfo buildPeerInfo(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            UserVM userVM = getUsers().get(peer.getPeerId());
            return JsPeerInfo.create(
                    JsPeer.create(peer),
                    userVM.getName().get(),
                    getSmallAvatarUrl(userVM.getAvatar().get()),
                    Placeholders.getPlaceholder(peer.getPeerId()));
        } else if (peer.getPeerType() == PeerType.GROUP) {
            GroupVM groupVM = getGroups().get(peer.getPeerId());
            return JsPeerInfo.create(
                    JsPeer.create(peer),
                    groupVM.getName().get(),
                    getSmallAvatarUrl(groupVM.getAvatar().get()),
                    Placeholders.getPlaceholder(peer.getPeerId()));
        } else {
            throw new RuntimeException();
        }
    }

    private String getSmallAvatarUrl(Avatar avatar) {
        if (avatar != null && avatar.getSmallImage() != null) {
            return getFileUrl(avatar.getSmallImage().getFileReference());
        }
        return null;
    }

    public String getFileUrl(FileReference fileReference) {
        return angularFilesModule.getFileUrl(fileReference.getFileId(), fileReference.getAccessHash());
    }
}