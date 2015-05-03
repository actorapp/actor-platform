/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.angular;

import java.util.HashMap;

import im.actor.model.entity.Avatar;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.js.JsMessenger;
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
import im.actor.model.viewmodel.UserTypingVM;
import im.actor.model.viewmodel.UserVM;

public class AngularModule extends BaseModule implements AngularFileLoadedListener {
    private JsMessenger messenger;
    private AngularList<JsDialog, Dialog> dialogsList;
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
            UserVM userVM = modules().getUsersModule().getUsersCollection().get(uid);
            final AngularValue<JsUser> value = new AngularValue<JsUser>(JsUser.fromUserVM(userVM));
            userVM.subscribe(new ModelChangedListener<UserVM>() {
                @Override
                public void onChanged(UserVM model) {
                    value.changeValue(JsUser.fromUserVM(model));
                }
            });
            users.put(uid, value);
        }
        return users.get(uid);
    }

    public AngularValue<JsGroup> getGroup(int gid) {
        if (!groups.containsKey(gid)) {
            GroupVM groupVM = modules().getGroupsModule().getGroupsCollection().get(gid);
            final AngularValue<JsGroup> value = new AngularValue<>(JsGroup.fromGroupVM(groupVM));
            groupVM.subscribe(new ModelChangedListener<GroupVM>() {
                @Override
                public void onChanged(GroupVM model) {
                    value.changeValue(JsGroup.fromGroupVM(model));
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
                                    .get(peer.getPeerId())
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
            boolean founded = false;
            for (Dialog dialog : dialogsList.getRawItems()) {
                Avatar avatar = dialog.getDialogAvatar();
                if (avatar != null && avatar.getSmallImage() != null &&
                        avatar.getSmallImage().getFileReference().getFileId() == fileId) {
                    founded = true;
                    break;
                }
            }
            if (founded) {
                dialogsList.forceReconvert();
            }
        }

        for (AngularList<JsMessage, Message> messageList : messagesList.values()) {
            boolean founded = false;
            for (Message message : messageList.getRawItems()) {
                UserVM user = modules().getUsersModule().getUsersCollection().get(message.getSenderId());
                Avatar avatar = user.getAvatar().get();
                if (avatar != null && avatar.getSmallImage() != null &&
                        avatar.getSmallImage().getFileReference().getFileId() == fileId) {
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
    }
}
