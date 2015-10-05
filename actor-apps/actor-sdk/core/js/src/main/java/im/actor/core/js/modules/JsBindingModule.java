/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.modules;

import java.util.HashMap;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.FileRemoteSource;
import im.actor.core.js.JsMessenger;
import im.actor.core.js.entity.JsContact;
import im.actor.core.js.entity.JsCounter;
import im.actor.core.js.entity.JsDialog;
import im.actor.core.js.entity.JsGroup;
import im.actor.core.js.entity.JsMessage;
import im.actor.core.js.entity.JsTyping;
import im.actor.core.js.entity.JsUser;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.core.viewmodel.AppStateVM;
import im.actor.core.viewmodel.GroupTypingVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserPresence;
import im.actor.core.viewmodel.UserTypingVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.js.mvvm.JsDisplayList;
import im.actor.runtime.mvvm.ModelChangedListener;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

public class JsBindingModule extends AbsModule implements JsFileLoadedListener {
    private JsMessenger messenger;
    private JsFilesModule filesModule;

    private HashMap<Integer, JsBindedValue<JsUser>> users = new HashMap<Integer, JsBindedValue<JsUser>>();
    private HashMap<Integer, JsBindedValue<JsGroup>> groups = new HashMap<Integer, JsBindedValue<JsGroup>>();
    private HashMap<Peer, JsBindedValue<JsTyping>> typing = new HashMap<Peer, JsBindedValue<JsTyping>>();
    private JsBindedValue<String> onlineState;

    private JsDisplayList<JsDialog, Dialog> dialogsList;
    private JsDisplayList<JsContact, Contact> contactsList;
    private HashMap<Peer, JsDisplayList<JsMessage, Message>> messageLists = new HashMap<Peer, JsDisplayList<JsMessage, Message>>();

    private JsBindedValue<JsCounter> globalCounter;
    private JsBindedValue<JsCounter> tempGlobalCounter;

    public JsBindingModule(JsMessenger messenger, JsFilesModule filesModule, Modules modules) {
        super(modules);

        this.filesModule = filesModule;
        this.messenger = messenger;
        this.filesModule.registerListener(this);
    }

    public JsBindedValue<String> getOnlineStatus() {
        if (onlineState == null) {

            final AppStateVM vm = context().getAppStateModule().getAppStateVM();
            onlineState = new JsBindedValue<String>("online");

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
            final JsBindedValue<JsUser> value = new JsBindedValue<JsUser>(JsUser.fromUserVM(userVM, messenger));

            // Bind updates
            userVM.subscribe(new ModelChangedListener<UserVM>() {
                @Override
                public void onChanged(UserVM model) {
                    value.changeValue(JsUser.fromUserVM(userVM, messenger));
                }
            }, false);

            // Sign for presence separately
            userVM.getPresence().subscribe(new ValueChangedListener<UserPresence>() {
                @Override
                public void onChanged(UserPresence val, Value<UserPresence> valueModel) {
                    value.changeValue(JsUser.fromUserVM(userVM, messenger));
                }
            }, false);
            users.put(uid, value);
        }
        return users.get(uid);
    }

    public JsBindedValue<JsGroup> getGroup(int gid) {
        if (!groups.containsKey(gid)) {
            final GroupVM groupVM = context().getGroupsModule().getGroupsCollection().get(gid);
            final JsBindedValue<JsGroup> value = new JsBindedValue<JsGroup>(JsGroup.fromGroupVM(groupVM, messenger));

            // Bind updates
            groupVM.subscribe(new ModelChangedListener<GroupVM>() {
                @Override
                public void onChanged(GroupVM model) {
                    value.changeValue(JsGroup.fromGroupVM(groupVM, messenger));
                }
            }, false);

            // Sign for presence separately
            groupVM.getPresence().subscribe(new ValueChangedListener<Integer>() {
                @Override
                public void onChanged(Integer val, Value<Integer> valueModel) {
                    value.changeValue(JsGroup.fromGroupVM(groupVM, messenger));
                }
            }, false);
            groups.put(gid, value);
        }
        return groups.get(gid);
    }

    public JsBindedValue<JsTyping> getTyping(final Peer peer) {
        if (!typing.containsKey(peer)) {
            if (peer.getPeerType() == PeerType.PRIVATE) {
                UserTypingVM userTypingVM = context().getTypingModule().getTyping(peer.getPeerId());

                final JsBindedValue<JsTyping> value = new JsBindedValue<JsTyping>();
                userTypingVM.getTyping().subscribe(new ValueChangedListener<Boolean>() {
                    @Override
                    public void onChanged(Boolean val, Value<Boolean> valueModel) {
                        String typingValue = null;
                        if (val) {
                            typingValue = messenger.getFormatter().formatTyping("");
                        }
                        value.changeValue(JsTyping.create(typingValue));
                    }
                });
                typing.put(peer, value);
            } else if (peer.getPeerType() == PeerType.GROUP) {
                GroupTypingVM groupTypingVM = context().getTypingModule().getGroupTyping(peer.getPeerId());
                final JsBindedValue<JsTyping> value = new JsBindedValue<JsTyping>();
                groupTypingVM.getActive().subscribe(new ValueChangedListener<int[]>() {
                    @Override
                    public void onChanged(int[] val, Value<int[]> valueModel) {
                        String typingValue = null;
                        if (val.length == 1) {
                            typingValue = messenger.getFormatter().formatTyping(context()
                                    .getUsersModule()
                                    .getUsers()
                                    .get(val[0])
                                    .getName()
                                    .get());
                        } else if (val.length > 1) {
                            typingValue = messenger.getFormatter().formatTyping(val.length);
                        }
                        value.changeValue(JsTyping.create(typingValue));
                    }
                });
                typing.put(peer, value);
            } else {
                throw new RuntimeException();
            }
        }
        return typing.get(peer);
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
            ValueModel<Integer> counter = context().getAppStateModule().getAppStateVM().getGlobalCounter();
            globalCounter = new JsBindedValue<JsCounter>(JsCounter.create(counter.get()));
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
            ValueModel<Integer> counter = context().getAppStateModule().getAppStateVM().getGlobalTempCounter();
            tempGlobalCounter = new JsBindedValue<JsCounter>(JsCounter.create(counter.get()));
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
    public void onFileLoaded(long fileId) {
        if (dialogsList != null) {
            for (Dialog dialog : dialogsList.getRawItems()) {
                if (checkAvatar(dialog.getDialogAvatar(), fileId)) {
                    dialogsList.forceReconvert(dialog.getEngineId());
                }
            }
        }

        if (contactsList != null) {
            for (Contact contact : contactsList.getRawItems()) {
                if (checkAvatar(contact.getAvatar(), fileId)) {
                    contactsList.forceReconvert(contact.getEngineId());
                }
            }
        }

        for (JsDisplayList<JsMessage, Message> messageList : messageLists.values()) {
            boolean founded = false;
            for (Message message : messageList.getRawItems()) {
                UserVM user = context().getUsersModule().getUsers().get(message.getSenderId());
                if (checkAvatar(user.getAvatar().get(), fileId)) {
                    founded = true;
                    break;
                }
                if (message.getContent() instanceof DocumentContent) {
                    DocumentContent doc = (DocumentContent) message.getContent();
                    if (doc.getSource() instanceof FileRemoteSource) {
                        if (((FileRemoteSource) doc.getSource()).getFileReference().getFileId() == fileId) {
                            founded = true;
                            break;
                        }
                    }
                }
            }
            if (founded) {
                messageList.forceReconvert();
            }
        }

        for (JsBindedValue<JsUser> u : users.values()) {
            int uid = u.get().getUid();
            UserVM userVM = context().getUsersModule().getUsers().get(uid);
            if (checkAvatar(userVM.getAvatar().get(), fileId)) {
                u.changeValue(JsUser.fromUserVM(userVM, messenger));
            }
        }

        for (JsBindedValue<JsGroup> g : groups.values()) {
            int gid = g.get().getGid();
            GroupVM groupVM = context().getGroupsModule().getGroupsCollection().get(gid);
            if (checkAvatar(groupVM.getAvatar().get(), fileId)) {
                g.changeValue(JsGroup.fromGroupVM(groupVM, messenger));
            }
        }
    }

    protected boolean checkAvatar(Avatar avatar, long fileId) {
        if (avatar == null) {
            return false;
        }
        if (avatar.getSmallImage() != null && avatar.getSmallImage().getFileReference().getFileId() == fileId) {
            return true;
        }
        if (avatar.getFullImage() != null && avatar.getFullImage().getFileReference().getFileId() == fileId) {
            return true;
        }
        if (avatar.getLargeImage() != null && avatar.getLargeImage().getFileReference().getFileId() == fileId) {
            return true;
        }
        return false;
    }
}
