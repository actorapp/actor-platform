/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.modules;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.SearchEntity;
import im.actor.core.entity.Sticker;
import im.actor.core.entity.StickerPack;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.FileRemoteSource;
import im.actor.core.entity.content.StickerContent;
import im.actor.core.js.JsMessenger;
import im.actor.core.js.entity.JsBlockedUser;
import im.actor.core.js.entity.JsCall;
import im.actor.core.js.entity.JsContact;
import im.actor.core.js.entity.JsCounter;
import im.actor.core.js.entity.JsDialog;
import im.actor.core.js.entity.JsDialogGroup;
import im.actor.core.js.entity.JsDialogShort;
import im.actor.core.js.entity.JsEventBusCallback;
import im.actor.core.js.entity.JsGroup;
import im.actor.core.js.entity.JsMessage;
import im.actor.core.js.entity.JsOnlineGroup;
import im.actor.core.js.entity.JsOnlineUser;
import im.actor.core.js.entity.JsSearchEntity;
import im.actor.core.js.entity.JsSticker;
import im.actor.core.js.entity.JsTyping;
import im.actor.core.js.entity.JsUser;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.core.viewmodel.AppStateVM;
import im.actor.core.viewmodel.CallMember;
import im.actor.core.viewmodel.CallState;
import im.actor.core.viewmodel.CallVM;
import im.actor.core.viewmodel.DialogGroup;
import im.actor.core.viewmodel.DialogSmall;
import im.actor.core.viewmodel.GlobalStateVM;
import im.actor.core.viewmodel.GroupTypingVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserPresence;
import im.actor.core.viewmodel.UserTypingVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.js.mvvm.JsDisplayList;
import im.actor.runtime.js.mvvm.JsDisplayListBind;
import im.actor.runtime.mvvm.ModelChangedListener;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

public class JsBindingModule extends AbsModule implements JsFileLoadedListener {

    private JsMessenger messenger;

    private HashMap<Integer, JsBindedValue<JsUser>> users = new HashMap<>();
    private HashMap<Integer, JsBindedValue<JsGroup>> groups = new HashMap<>();
    private HashMap<Integer, JsBindedValue<JsOnlineUser>> usersOnlines = new HashMap<>();
    private HashMap<Integer, JsBindedValue<JsBlockedUser>> usersBloked = new HashMap<>();
    private HashMap<Integer, JsBindedValue<JsOnlineGroup>> groupOnlines = new HashMap<>();
    private HashMap<Peer, JsBindedValue<JsTyping>> typing = new HashMap<>();
    private JsBindedValue<String> onlineState;

    private JsDisplayList<JsDialog, Dialog> dialogsList;
    private JsDisplayList<JsContact, Contact> contactsList;
    private JsDisplayList<JsSearchEntity, SearchEntity> searchList;
    private HashMap<Peer, JsDisplayList<JsMessage, Message>> messageLists = new HashMap<>();

    private JsBindedValue<JsCounter> globalCounter;
    private JsBindedValue<JsCounter> tempGlobalCounter;

    private JsBindedValue<JsArray<JsDialogGroup>> dialogsGroupedList;

    private HashMap<String, JsBindedValue<JsCall>> calls = new HashMap<>();

    private ArrayList<JsEventBusCallback> eventBusCallbacks = new ArrayList<>();

    private JsBindedValue<JsArray<JsSticker>> stickers;

    public JsBindingModule(JsMessenger messenger, JsFilesModule filesModule, Modules modules) {
        super(modules);

        this.messenger = messenger;
        filesModule.registerListener(this);
    }

    public void broadcastEvent(String tag, JavaScriptObject obj) {
        for (JsEventBusCallback c : eventBusCallbacks) {
            c.onEvent(tag, obj);
        }
    }

    public void subscribeEventBus(JsEventBusCallback callback) {
        if (!eventBusCallbacks.contains(callback)) {
            eventBusCallbacks.add(callback);
        }
    }

    public void unsubscribeEventBus(JsEventBusCallback callback) {
        eventBusCallbacks.remove(callback);
    }

    public JsBindedValue<JsArray<JsDialogGroup>> getDialogsGroupedList() {
        if (dialogsGroupedList == null) {
            ValueModel<ArrayList<DialogGroup>> dialogGroups =
                    context().getMessagesModule().getDialogGroupsVM().getGroupsValueModel();
            dialogsGroupedList = new JsBindedValue<>();
            dialogGroups.subscribe(new ValueChangedListener<ArrayList<DialogGroup>>() {
                @Override
                public void onChanged(ArrayList<DialogGroup> val, Value<ArrayList<DialogGroup>> valueModel) {
                    if (val == null) {
                        dialogsGroupedList.changeValue(JsArray.createArray().<JsArray<JsDialogGroup>>cast());
                    } else {
                        JsArray<JsDialogGroup> res = JsArray.createArray().cast();
                        for (DialogGroup g : val) {
                            JsArray<JsDialogShort> resd = JsArray.createArray().cast();
                            for (DialogSmall ds : g.getDialogs()) {
                                resd.push(JsDialogShort.create(messenger.buildPeerInfo(ds.getPeer()), ds.getCounter()));
                            }
                            res.push(JsDialogGroup.create(g.getTitle(), g.getKey(), resd));
                        }
                        dialogsGroupedList.changeValue(res);
                    }
                }
            });
        }

        return dialogsGroupedList;
    }

    public JsBindedValue<JsArray<JsSticker>> getStickers() {
        if (stickers == null) {
            stickers = new JsBindedValue<>((JsArray<JsSticker>) JsArray.createArray());
            messenger.getAvailableStickersVM().getOwnStickerPacks().subscribe(new ValueChangedListener<ArrayList<StickerPack>>() {
                @Override
                public void onChanged(ArrayList<StickerPack> val, Value<ArrayList<StickerPack>> valueModel) {
                    JsArray<JsSticker> stickerJsArray = JsArray.createArray().cast();
                    stickerJsArray.setLength(0);
                    for (StickerPack pack : val) {
                        for (Sticker sticker : pack.getStickers()) {
                            stickerJsArray.push(JsSticker.create(sticker));
                        }
                    }
                    stickers.changeValue(stickerJsArray);
                }
            });
        }
        return stickers;
    }

    public JsBindedValue<String> getOnlineStatus() {
        if (onlineState == null) {

            final GlobalStateVM vm = context().getAppStateModule().getGlobalStateVM();
            onlineState = new JsBindedValue<>("online");

            vm.getIsConnecting().subscribe(new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> valueModel) {
                    if (val) {
                        onlineState.changeValue("connecting");
                    } else {
                        if (vm.getIsSyncing().get()) {
                            onlineState.changeValue("updating");
                        } else {
                            onlineState.changeValue("online");
                        }
                    }
                }
            });
            vm.getIsSyncing().subscribe(new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> valueModel) {
                    if (vm.getIsConnecting().get()) {
                        onlineState.changeValue("connecting");
                    } else {
                        if (val) {
                            onlineState.changeValue("updating");
                        } else {
                            onlineState.changeValue("online");
                        }
                    }
                }
            });
        }

        return onlineState;
    }

    public JsBindedValue<JsUser> getUser(int uid) {
        if (!users.containsKey(uid)) {
            final UserVM userVM = context().getUsersModule().getUsers().get(uid);
            final JsBindedValue<JsUser> value = new JsBindedValue<>(JsUser.fromUserVM(userVM, messenger));

            // Bind updates
            userVM.subscribe(new ModelChangedListener<UserVM>() {
                @Override
                public void onChanged(UserVM model) {
                    value.changeValue(JsUser.fromUserVM(userVM, messenger));
                }
            }, false);

            // Sign for contact separately
            userVM.isContact().subscribe(new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> valueModel) {
                    value.changeValue(JsUser.fromUserVM(userVM, messenger));
                }
            });

            // Sign for blocked separately
            userVM.getIsBlocked().subscribe(new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> valueModel) {
                    value.changeValue(JsUser.fromUserVM(userVM, messenger));
                }
            });

            users.put(uid, value);
        }
        return users.get(uid);
    }

    public JsBindedValue<JsOnlineUser> getUserOnline(int uid) {
        if (!usersOnlines.containsKey(uid)) {
            final JsBindedValue<JsOnlineUser> value = new JsBindedValue<>();
            final UserVM userVM = context().getUsersModule().getUsers().get(uid);

            userVM.getPresence().subscribe(new ValueChangedListener<UserPresence>() {
                @Override
                public void onChanged(UserPresence val, Value<UserPresence> valueModel) {
                    if (val.getState() == UserPresence.State.UNKNOWN) {
                        value.changeValue(null);
                    } else {
                        String presenceString = messenger.getFormatter().formatPresence(val, userVM.getSex());
                        if (userVM.isBot()) {
                            presenceString = "bot";
                        }
                        value.changeValue(JsOnlineUser.create(presenceString, val.getState() == UserPresence.State.ONLINE));
                    }
                }
            });

            usersOnlines.put(uid, value);
        }
        return usersOnlines.get(uid);
    }

    public JsBindedValue<JsBlockedUser> getUserBlocked(int uid) {
        if (!usersBloked.containsKey(uid)) {
            final JsBindedValue<JsBlockedUser> value = new JsBindedValue<>();
            final UserVM userVM = context().getUsersModule().getUsers().get(uid);

            userVM.getIsBlocked().subscribe(new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> valueModel) {
                    value.changeValue(JsBlockedUser.create(val));
                }
            });

            usersBloked.put(uid, value);
        }
        return usersBloked.get(uid);
    }

    public JsBindedValue<JsGroup> getGroup(int gid) {
        if (!groups.containsKey(gid)) {
            final GroupVM groupVM = context().getGroupsModule().getGroupsCollection().get(gid);
            final JsBindedValue<JsGroup> value = new JsBindedValue<>(JsGroup.fromGroupVM(groupVM, messenger));

            // Bind updates
            groupVM.subscribe(new ModelChangedListener<GroupVM>() {
                @Override
                public void onChanged(GroupVM model) {
                    value.changeValue(JsGroup.fromGroupVM(groupVM, messenger));
                }
            }, false);

            groups.put(gid, value);
        }
        return groups.get(gid);
    }

    public JsBindedValue<JsOnlineGroup> getGroupOnline(int gid) {
        if (!groupOnlines.containsKey(gid)) {
            final JsBindedValue<JsOnlineGroup> value = new JsBindedValue<>();
            final GroupVM groupVM = context().getGroupsModule().getGroupsCollection().get(gid);
            groupVM.getPresence().subscribe(new ValueChangedListener<Integer>() {
                @Override
                public void onChanged(Integer val, Value<Integer> valueModel) {
                    if (groupVM.isMember().get()) {
                        if (val == null) {
                            value.changeValue(null);
                            return;
                        }
                        String presence = messenger.getFormatter().formatGroupMembers(groupVM.getMembersCount());
                        if (val > 0) {
                            presence += ", " + messenger.getFormatter().formatGroupOnline(val);
                        }
                        value.changeValue(JsOnlineGroup.create(groupVM.getMembersCount(), val, presence, false));
                    } else {
                        value.changeValue(JsOnlineGroup.create(0, 0, "Not member", false));
                    }
                }
            });
            groupOnlines.put(gid, value);
        }
        return groupOnlines.get(gid);
    }

    public JsBindedValue<JsCall> getCall(String id) {
        if (!calls.containsKey(id)) {
            final CallVM callModel = messenger.getCall(Long.parseLong(id));
            final JsBindedValue<JsCall> jsCall = new JsBindedValue<>(JsCall.create(messenger, callModel));
            callModel.getState().subscribe(new ValueChangedListener<CallState>() {
                @Override
                public void onChanged(CallState val, Value<CallState> valueModel) {
                    jsCall.changeValue(JsCall.create(messenger, callModel));
                }
            });
            callModel.getMembers().subscribe(new ValueChangedListener<ArrayList<CallMember>>() {
                @Override
                public void onChanged(ArrayList<CallMember> val, Value<ArrayList<CallMember>> valueModel) {
                    jsCall.changeValue(JsCall.create(messenger, callModel));
                }
            });
            calls.put(id, jsCall);
        }
        return calls.get(id);
    }

    public JsBindedValue<JsTyping> getTyping(final Peer peer) {
        if (!typing.containsKey(peer)) {
            if (peer.getPeerType() == PeerType.PRIVATE) {
                UserTypingVM userTypingVM = context().getTypingModule().getTyping(peer.getPeerId());

                final JsBindedValue<JsTyping> value = new JsBindedValue<>();
                userTypingVM.getTyping().subscribe((val, valueModel) -> {
                    String typingValue = null;
                    if (val) {
                        typingValue = messenger.getFormatter().formatTyping("");
                    }
                    value.changeValue(JsTyping.create(typingValue));
                });
                typing.put(peer, value);
            } else if (peer.getPeerType() == PeerType.GROUP) {
                GroupTypingVM groupTypingVM = context().getTypingModule().getGroupTyping(peer.getPeerId());
                final JsBindedValue<JsTyping> value = new JsBindedValue<>();
                groupTypingVM.getActive().subscribe((val, valueModel) -> {
                    if (val.length > 0) {
                        ArrayList<String> names = new ArrayList<>();
                        for (int i : val) {
                            names.add(context()
                                    .getUsersModule()
                                    .getUsers()
                                    .get(i)
                                    .getName()
                                    .get());
                        }
                        value.changeValue(JsTyping.create(messenger.getFormatter().formatTyping(names)));
                    } else {
                        value.changeValue(JsTyping.create(null));
                    }
                });
                typing.put(peer, value);
            } else {
                throw new RuntimeException();
            }
        }
        return typing.get(peer);
    }

    public JsDisplayList<JsSearchEntity, SearchEntity> getSharedSearchList() {
        if (searchList == null) {
            searchList = (JsDisplayList<JsSearchEntity, SearchEntity>) context().getDisplayListsModule().buildSearchList(true);
        }
        return searchList;
    }

    public JsDisplayList<JsContact, Contact> getSharedContactList() {
        if (contactsList == null) {
            contactsList = (JsDisplayList<JsContact, Contact>) context().getDisplayListsModule().getContactsSharedList();
        }

        return contactsList;
    }

    public JsDisplayList<JsDialog, Dialog> getSharedDialogList() {
        if (dialogsList == null) {
            dialogsList = (JsDisplayList<JsDialog, Dialog>) context().getDisplayListsModule().getDialogsSharedList();
        }

        return dialogsList;
    }

    public JsDisplayList<JsMessage, Message> getSharedMessageList(Peer peer) {
        if (!messageLists.containsKey(peer)) {
            messageLists.put(peer,
                    (JsDisplayList<JsMessage, Message>) context().getDisplayListsModule().getMessagesSharedList(peer));
        }

        return messageLists.get(peer);
    }

    public JsBindedValue<JsCounter> getGlobalCounter() {
        if (globalCounter == null) {
            ValueModel<Integer> counter = context().getAppStateModule().getGlobalStateVM().getGlobalCounter();
            globalCounter = new JsBindedValue<>(JsCounter.create(counter.get()));
            counter.subscribe(new ValueChangedListener<Integer>() {
                @Override
                public void onChanged(Integer val, Value<Integer> valueModel) {
                    globalCounter.changeValue(JsCounter.create(val));
                }
            }, false);
        }
        return globalCounter;
    }

    public JsBindedValue<JsCounter> getTempGlobalCounter() {
        if (tempGlobalCounter == null) {
            ValueModel<Integer> counter = context().getAppStateModule().getGlobalStateVM().getGlobalTempCounter();
            tempGlobalCounter = new JsBindedValue<>(JsCounter.create(counter.get()));
            counter.subscribe(new ValueChangedListener<Integer>() {
                @Override
                public void onChanged(Integer val, Value<Integer> valueModel) {
                    tempGlobalCounter.changeValue(JsCounter.create(val));
                }
            }, false);
        }
        return tempGlobalCounter;
    }

    @Override
    public void onFileLoaded(HashSet<Long> fileId) {

        //
        // Dialogs List
        //

        if (dialogsList != null) {
            for (JsDisplayListBind<JsDialog, Dialog> b : dialogsList.getActiveBinds()) {
                b.startReconverting();
                for (Dialog dialog : b.getRawItems()) {
                    if (checkAvatar(dialog.getDialogAvatar(), fileId)) {
                        b.forceReconvert(dialog.getEngineId());
                    }
                }
                b.stopReconverting();
            }
        }

        //
        // Grouped Dialogs
        //

        if (dialogsGroupedList != null) {
            ArrayList<DialogGroup> groups = context().getMessagesModule().getDialogGroupsVM().getGroupsValueModel().get();
            if (groups != null) {
                outer:
                for (DialogGroup g : groups) {
                    for (DialogSmall ds : g.getDialogs()) {
                        if (checkAvatar(ds.getAvatar(), fileId)) {
                            context().getMessagesModule().getDialogGroupsVM().getGroupsValueModel().forceNotify();
                            break outer;
                        }
                    }
                }
            }
        }

        //
        // Contacts List
        //

        if (contactsList != null) {
            for (JsDisplayListBind<JsContact, Contact> b : contactsList.getActiveBinds()) {
                b.startReconverting();
                for (Contact contact : b.getRawItems()) {
                    if (checkAvatar(contact.getAvatar(), fileId)) {
                        b.forceReconvert(contact.getEngineId());
                    }
                }
                b.stopReconverting();
            }
        }

        //
        // Message Contents
        //

        for (JsDisplayList<JsMessage, Message> messageList : messageLists.values()) {
            for (JsDisplayListBind<JsMessage, Message> b : messageList.getActiveBinds()) {
                b.startReconverting();
                for (Message message : b.getRawItems()) {
                    UserVM user = context().getUsersModule().getUsers().get(message.getSenderId());
                    if (checkAvatar(user.getAvatar().get(), fileId)) {
                        b.forceReconvert(message.getEngineId());
                        continue;
                    }
                    if (message.getContent() instanceof DocumentContent) {
                        DocumentContent doc = (DocumentContent) message.getContent();
                        if (doc.getSource() instanceof FileRemoteSource) {
                            if (fileId.contains(((FileRemoteSource) doc.getSource()).getFileReference().getFileId())) {
                                b.forceReconvert(message.getEngineId());
                            }
                        }
                    }
                    if (message.getContent() instanceof StickerContent) {
                        StickerContent content = (StickerContent) message.getContent();
                        if (content.getImage512() != null) {
                            long stickerFileId = content.getImage512().getReference().getFileId();
                            if (fileId.contains(stickerFileId)) {
                                b.forceReconvert(message.getEngineId());
                            }
                        } else if (content.getImage256() != null) {
                            long stickerFileId = content.getImage256().getReference().getFileId();
                            if (fileId.contains(stickerFileId)) {
                                b.forceReconvert(message.getEngineId());
                            }
                        }
                    }
                }
                b.stopReconverting();
            }
        }

        //
        // Users
        //

        for (JsBindedValue<JsUser> u : users.values()) {
            int uid = u.get().getUid();
            UserVM userVM = context().getUsersModule().getUsers().get(uid);
            if (checkAvatar(userVM.getAvatar().get(), fileId)) {
                u.changeValue(JsUser.fromUserVM(userVM, messenger));
            }
        }

        //
        // Groups
        //

        for (JsBindedValue<JsGroup> g : groups.values()) {
            int gid = g.get().getGid();
            GroupVM groupVM = context().getGroupsModule().getGroupsCollection().get(gid);
            if (checkAvatar(groupVM.getAvatar().get(), fileId)) {
                g.changeValue(JsGroup.fromGroupVM(groupVM, messenger));
            }
        }

        //
        // Stickers
        //
        if (stickers != null) {
            outer:
            for (StickerPack stickerPack : messenger.getAvailableStickersVM().getOwnStickerPacks().get()) {
                for (Sticker s : stickerPack.getStickers()) {
                    if (s.getImage256() != null && fileId.contains(s.getImage256().getFileId())) {
                        messenger.getAvailableStickersVM().getOwnStickerPacks().forceNotify();
                        break outer;
                    }
                }
            }
        }
    }

    protected boolean checkAvatar(Avatar avatar, HashSet<Long> fileIds) {
        if (avatar == null) {
            return false;
        }
        if (avatar.getSmallImage() != null && fileIds.contains(avatar.getSmallImage().getFileReference().getFileId())) {
            return true;
        }
        if (avatar.getFullImage() != null && fileIds.contains(avatar.getFullImage().getFileReference().getFileId())) {
            return true;
        }
        if (avatar.getLargeImage() != null && fileIds.contains(avatar.getLargeImage().getFileReference().getFileId())) {
            return true;
        }
        return false;
    }
}
