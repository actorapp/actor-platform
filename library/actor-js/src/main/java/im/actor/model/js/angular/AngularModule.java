/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.angular;

import java.util.HashMap;

import im.actor.model.entity.Avatar;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.js.JsMessenger;
import im.actor.model.js.entity.JsContact;
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.entity.JsGroup;
import im.actor.model.js.entity.JsMessage;
import im.actor.model.js.entity.JsTyping;
import im.actor.model.js.entity.JsUser;
import im.actor.model.js.providers.storage.JsListEngine;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.GroupTypingVM;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserPresence;
import im.actor.model.viewmodel.UserTypingVM;
import im.actor.model.viewmodel.UserVM;

public class AngularModule extends BaseModule implements AngularFileLoadedListener {
    private JsMessenger messenger;
    private AngularList<JsDialog, Dialog> dialogsList;
    private AngularList<JsContact, Contact> contactsList;
    private AngularFilesModule filesModule;

    private HashMap<Peer, AngularList<JsMessage, Message>> messagesList = new HashMap<Peer, AngularList<JsMessage, Message>>();
    private HashMap<Integer, AngularValue<JsUser>> users = new HashMap<Integer, AngularValue<JsUser>>();
    private HashMap<Integer, AngularValue<JsGroup>> groups = new HashMap<Integer, AngularValue<JsGroup>>();
    private HashMap<Peer, AngularValue<JsTyping>> typing = new HashMap<Peer, AngularValue<JsTyping>>();

    public AngularModule(JsMessenger messenger, AngularFilesModule filesModule, Modules modules) {
        super(modules);
        this.filesModule = filesModule;
        this.messenger = messenger;
        this.filesModule.registerListener(this);
    }

    public AngularList<JsContact, Contact> getContactsList() {
        if (contactsList == null) {
            contactsList = new AngularList<JsContact, Contact>(
                    (JsListEngine<Contact>) modules().getContactsModule().getContacts(),
                    false, JsContact.CONVERTER, messenger);
        }
        return contactsList;
    }

    public AngularList<JsDialog, Dialog> getDialogsList() {
        if (dialogsList == null) {
            dialogsList = new AngularList<JsDialog, Dialog>((JsListEngine<Dialog>) modules().getMessagesModule().getDialogsEngine(),
                    false, JsDialog.CONVERTER, messenger);
        }
        return dialogsList;
    }

    public AngularList<JsMessage, Message> getMessagesList(Peer peer) {
        if (!messagesList.containsKey(peer)) {
            messagesList.put(peer, new AngularList<JsMessage, Message>(
                    (JsListEngine<Message>) modules().getMessagesModule().getConversationEngine(peer),
                    true, JsMessage.CONVERTER, messenger));
        }
        return messagesList.get(peer);
    }

    public AngularValue<JsUser> getUser(int uid) {
        if (!users.containsKey(uid)) {
            final UserVM userVM = modules().getUsersModule().getUsersCollection().get(uid);
            final AngularValue<JsUser> value = new AngularValue<JsUser>(JsUser.fromUserVM(userVM, messenger));
            userVM.subscribe(new ModelChangedListener<UserVM>() {
                @Override
                public void onChanged(UserVM model) {
                    value.changeValue(JsUser.fromUserVM(userVM, messenger));
                }
            });
            userVM.getPresence().subscribe(new ValueChangedListener<UserPresence>() {
                @Override
                public void onChanged(UserPresence val, ValueModel<UserPresence> valueModel) {
                    value.changeValue(JsUser.fromUserVM(userVM, messenger));
                }
            });
            users.put(uid, value);
        }
        return users.get(uid);
    }

    public AngularValue<JsGroup> getGroup(int gid) {
        if (!groups.containsKey(gid)) {
            final GroupVM groupVM = modules().getGroupsModule().getGroupsCollection().get(gid);
            final AngularValue<JsGroup> value = new AngularValue<>(JsGroup.fromGroupVM(groupVM, messenger));
            groupVM.subscribe(new ModelChangedListener<GroupVM>() {
                @Override
                public void onChanged(GroupVM model) {
                    value.changeValue(JsGroup.fromGroupVM(groupVM, messenger));
                }
            });
            groupVM.getPresence().subscribe(new ValueChangedListener<Integer>() {
                @Override
                public void onChanged(Integer val, ValueModel<Integer> valueModel) {
                    value.changeValue(JsGroup.fromGroupVM(groupVM, messenger));
                }
            });
            groups.put(gid, value);
        }
        return groups.get(gid);
    }

    public AngularValue<JsTyping> getTyping(final Peer peer) {
        if (!typing.containsKey(peer)) {
            if (peer.getPeerType() == PeerType.PRIVATE) {
                UserTypingVM userTypingVM = modules().getTypingModule().getTyping(peer.getPeerId());

                final AngularValue<JsTyping> value = new AngularValue<>();
                userTypingVM.getTyping().subscribe(new ValueChangedListener<Boolean>() {
                    @Override
                    public void onChanged(Boolean val, ValueModel<Boolean> valueModel) {
                        String typingValue = null;
                        if (val) {
                            typingValue = messenger.getFormatter().formatTyping("");
                        }
                        value.changeValue(JsTyping.create(typingValue));
                    }
                });
                typing.put(peer, value);
            } else if (peer.getPeerType() == PeerType.GROUP) {
                GroupTypingVM groupTypingVM = modules().getTypingModule().getGroupTyping(peer.getPeerId());
                final AngularValue<JsTyping> value = new AngularValue<>();
                groupTypingVM.getActive().subscribe(new ValueChangedListener<int[]>() {
                    @Override
                    public void onChanged(int[] val, ValueModel<int[]> valueModel) {
                        String typingValue = null;
                        if (val.length == 1) {
                            typingValue = messenger.getFormatter().formatTyping(modules()
                                    .getUsersModule()
                                    .getUsersCollection()
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

    @Override
    public void onFileLoaded(long fileId) {
        if (dialogsList != null) {
            for (Dialog dialog : dialogsList.getRawItems()) {
                if (checkAvatar(dialog.getDialogAvatar(), fileId)) {
                    dialogsList.forceReconvert(dialog.getEngineId());
                }
            }
        }

        for (AngularList<JsMessage, Message> messageList : messagesList.values()) {
            boolean founded = false;
            for (Message message : messageList.getRawItems()) {
                UserVM user = modules().getUsersModule().getUsersCollection().get(message.getSenderId());
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

        for (AngularValue<JsUser> u : users.values()) {
            int uid = u.get().getUid();
            UserVM userVM = modules().getUsersModule().getUsersCollection().get(uid);
            if (checkAvatar(userVM.getAvatar().get(), fileId)) {
                u.changeValue(JsUser.fromUserVM(userVM, messenger));
            }
        }

        for (AngularValue<JsGroup> g : groups.values()) {
            int gid = g.get().getGid();
            GroupVM groupVM = modules().getGroupsModule().getGroupsCollection().get(gid);
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
