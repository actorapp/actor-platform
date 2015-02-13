package im.actor.messenger.core.actors.chat;

import com.droidkit.actors.typed.TypedActor;
import com.droidkit.bser.Bser;
import com.droidkit.engine.list.ListEngine;
import com.droidkit.engine.search.SearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.api.scheme.Dialog;
import im.actor.api.scheme.FileExPhoto;
import im.actor.api.scheme.FileExVideo;
import im.actor.api.scheme.FileMessage;
import im.actor.api.scheme.MessageContent;
import im.actor.api.scheme.PeerType;
import im.actor.api.scheme.ServiceMessage;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.ProfileSyncState;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.storage.DialogStorage;
import im.actor.messenger.storage.MessageType;
import im.actor.messenger.storage.scheme.*;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.groups.GroupInfo;
import im.actor.messenger.storage.scheme.messages.ConversationMessage;
import im.actor.messenger.storage.scheme.messages.DialogItem;
import im.actor.messenger.storage.scheme.messages.DialogItemBuilder;
import im.actor.messenger.storage.scheme.messages.FastThumb;
import im.actor.messenger.storage.scheme.messages.MessageState;
import im.actor.messenger.storage.scheme.messages.types.AudioMessage;
import im.actor.messenger.storage.scheme.messages.types.DocumentMessage;
import im.actor.messenger.storage.scheme.messages.types.GroupAdd;
import im.actor.messenger.storage.scheme.messages.types.GroupAvatar;
import im.actor.messenger.storage.scheme.messages.types.GroupCreated;
import im.actor.messenger.storage.scheme.messages.types.GroupKick;
import im.actor.messenger.storage.scheme.messages.types.GroupLeave;
import im.actor.messenger.storage.scheme.messages.types.GroupTitle;
import im.actor.messenger.storage.scheme.messages.types.PhotoMessage;
import im.actor.messenger.storage.scheme.messages.types.TextMessage;
import im.actor.messenger.storage.scheme.messages.types.UserAddedDeviceMessage;
import im.actor.messenger.storage.scheme.messages.types.UserRegisteredMessage;
import im.actor.messenger.storage.scheme.messages.types.VideoMessage;

import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;
import static im.actor.messenger.storage.ListEngines.getChatsListEngine;
import static im.actor.messenger.storage.SearchEngines.userSearchEngine;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class DialogsActor extends TypedActor<DialogsInt> implements DialogsInt {

    private static final TypedActorHolder<DialogsInt> HOLDER = new TypedActorHolder<DialogsInt>(
            DialogsInt.class, DialogsActor.class, "dialogs"
    );

    public static DialogsInt dialogs() {
        return HOLDER.get();
    }

    private ListEngine<DialogItem> listEngine;
    private SearchEngine<GlobalSearch> searchEngine;

    public DialogsActor() {
        super(DialogsInt.class);
    }

    @Override
    public void preStart() {
        listEngine = getChatsListEngine();
        searchEngine = userSearchEngine();
    }

    @Override
    public void onDialogsHistoryLoaded(List<Dialog> dialogs) {
        ArrayList<DialogItem> updated = new ArrayList<DialogItem>();
        for (Dialog d : dialogs) {
            int chatId = d.getPeer().getId();
            int chatType;
            String title;
            Avatar avatar;
            if (d.getPeer().getType() == PeerType.GROUP) {
                chatType = DialogType.TYPE_GROUP;
                GroupModel groupModel = groups().get(chatId);
                title = groupModel.getTitle();
                avatar = groupModel.getAvatarModel().getValue();
            } else if (d.getPeer().getType() == PeerType.PRIVATE) {
                chatType = DialogType.TYPE_USER;
                UserModel userModel = users().get(chatId);
                title = userModel.getName();
                avatar = userModel.getAvatar().getValue();
            } else {
                continue;
            }

            DialogItem item = listEngine.getValue(DialogUids.getDialogUid(chatType, chatId));
            if (item == null) {
                MessageDesc desc = createDesc(d.getMessage());
                DialogItem dialogItem = new DialogItemBuilder()
                        .setType(chatType)
                        .setId(chatId)
                        .setDialogTitle(title)
                        .setAvatar(avatar)
                        .setUnreadCount(d.getUnreadCount())

                        .setRid(d.getRid())
                        .setSortKey(d.getSortDate())
                        .setTime(d.getDate())
                        .setMessageType(desc.getType())
                        .setText(desc.getText())
                        .setStatus(MessageState.SENT)
                        .setSenderId(d.getSenderUid())
                        .setRelatedUid(desc.getRelatedUid())

                        .createDialogItem();

                searchEngine.index(DialogUids.getDialogUid(chatType, chatId),
                        d.getSortDate(),
                        title, new GlobalSearch(chatType, chatId, title, avatar));

                updated.add(dialogItem);
            }
        }

        listEngine.addOrUpdateItems(updated);


    }

    @Override
    public void onMessageArrived(int type, int id, ConversationMessage conversationMessage) {

        MessageDesc desc = createDesc(conversationMessage);

        String name;
        Avatar avatar;

        if (type == DialogType.TYPE_USER) {
            UserModel u = users().get(id);
            if (u == null) {
                return;
            }
            name = u.getName();
            avatar = u.getAvatar().getValue();
        } else if (type == DialogType.TYPE_GROUP) {
            GroupModel info = groups().get(id);
            if (info == null) {
                return;
            }
            name = info.getTitle();
            avatar = null;
        } else {
            return;
        }

        DialogItem item = listEngine.getValue(DialogUids.getDialogUid(type, id));

        DialogItem dialogItem;

        if (item == null) {
            if (desc.isSilent) {
                return;
            }
            dialogItem = new DialogItemBuilder()
                    .setType(type)
                    .setId(id)
                    .setDialogTitle(name)
                    .setAvatar(avatar)
                    .setUnreadCount(0)

                    .setRid(conversationMessage.getRid())
                    .setSortKey(conversationMessage.getSortKey())
                    .setTime(conversationMessage.getTime())
                    .setMessageType(desc.type)
                    .setText(desc.text)
                    .setStatus(conversationMessage.getMessageState())
                    .setSenderId(conversationMessage.getSenderId())
                    .setRelatedUid(desc.getRelatedUid())

                    .createDialogItem();
        } else if (item.getSortKey() <= conversationMessage.getSortKey()) {
            long sKey = desc.isSilent ? item.getSortKey() : conversationMessage.getSortKey();

            dialogItem = new DialogItemBuilder(item)
                    .setRid(conversationMessage.getRid())
                    .setSortKey(sKey)
                    .setTime(conversationMessage.getTime())
                    .setMessageType(desc.type)
                    .setText(desc.text)
                    .setStatus(conversationMessage.getMessageState())
                    .setSenderId(conversationMessage.getSenderId())
                    .setRelatedUid(desc.getRelatedUid())
                    .createDialogItem();
        } else {
            return;
        }

        listEngine.addOrUpdateItem(dialogItem);

        searchEngine.index(DialogUids.getDialogUid(type, id),
                conversationMessage.getSortKey(),
                name, new GlobalSearch(type, id, name, avatar));

        ProfileSyncState.onDialogsNotEmpty();
    }

    @Override
    public void onMessageStateChanged(int type, int id, long rid, MessageState messageState) {
        DialogItem item = listEngine.getValue(DialogUids.getDialogUid(type, id));
        if (item != null) {
            if (item.getRid() == rid) {
                listEngine.addOrUpdateItem(new DialogItemBuilder(item)
                        .setStatus(messageState)
                        .createDialogItem());
            }
        }
    }

    @Override
    public void onUserChangedName(int uid, String name) {
        DialogItem item = listEngine.getValue(DialogUids.getDialogUid(DialogType.TYPE_USER, uid));
        if (item != null) {
            DialogItem itm2 = new DialogItemBuilder(item)
                    .setDialogTitle(name)
                    .createDialogItem();
            listEngine.addOrUpdateItem(itm2);

            index(itm2);
        }
    }

    @Override
    public void onUserChangedAvatar(int uid, Avatar avatar) {
        DialogItem item = listEngine.getValue(DialogUids.getDialogUid(0, uid));
        if (item != null) {
            DialogItem itm2 = new DialogItemBuilder(item)
                    .setAvatar(avatar)
                    .createDialogItem();
            listEngine.addOrUpdateItem(itm2);

            index(itm2);
        }
    }

    @Override
    public void onGroupChangedTitle(int groupId, String title) {
        DialogItem item = listEngine.getValue(DialogUids.getDialogUid(DialogType.TYPE_GROUP, groupId));
        if (item != null) {
            DialogItem itm2 = new DialogItemBuilder(item)
                    .setDialogTitle(title)
                    .createDialogItem();
            listEngine.addOrUpdateItem(itm2);

            index(itm2);
        }
    }

    @Override
    public void onGroupChangedAvatar(int groupId, Avatar avatar) {
        DialogItem item = listEngine.getValue(DialogUids.getDialogUid(DialogType.TYPE_GROUP, groupId));
        if (item != null) {
            DialogItem itm2 = new DialogItemBuilder(item)
                    .setAvatar(avatar)
                    .createDialogItem();
            listEngine.addOrUpdateItem(itm2);

            index(itm2);
        }
    }

    @Override
    public void onCounterChanged(int type, int id, int value) {
        DialogItem item = listEngine.getValue(DialogUids.getDialogUid(type, id));
        if (item == null) {
            return;
        }
        listEngine.addOrUpdateItem(new DialogItemBuilder(item)
                .setUnreadCount(value)
                .createDialogItem());
    }

    // Deletion

    @Override
    public void onChatClear(int type, int id) {
        DialogItem item = listEngine.getValue(DialogUids.getDialogUid(type, id));
        if (item != null) {
            listEngine.addOrUpdateItem(new DialogItemBuilder(item)
                    .setText("")
                    .setMessageType(MessageType.EMPTY)
                    .setSenderId(0)
                    .setStatus(MessageState.PENDING)
                    .createDialogItem());
        }
    }

    @Override
    public void onDialogDelete(int type, int id) {
        listEngine.removeItem(DialogUids.getDialogUid(type, id));
        if (type == DialogType.TYPE_GROUP) {
            removeIndex(DialogUids.getDialogUid(type, id));
        }
    }

    @Override
    public void onDeleteMessages(int type, int id, long[] rids, ConversationMessage first) {
        DialogItem item = listEngine.getValue(DialogUids.getDialogUid(type, id));
        if (item != null) {
            boolean contains = false;
            for (long r : rids) {
                if (r == item.getRid()) {
                    contains = true;
                    break;
                }
            }
            if (contains) {
                if (first == null) {
                    onChatClear(type, id);
                } else {
                    MessageDesc desc = createDesc(first);

                    DialogItem dialogItem = new DialogItemBuilder(item)
                            .setRid(first.getRid())
//                             .setSortKey(first.getSortKey())
                            .setTime(first.getTime())
                            .setStatus(first.getMessageState())
                            .setMessageType(desc.type)
                            .setSenderId(first.getSenderId())
                            .setText(desc.text)
                            .setRelatedUid(desc.getRelatedUid())
                            .createDialogItem();

                    listEngine.addOrUpdateItem(dialogItem);
                }
            }
        }
    }

    private void index(DialogItem dialogItem) {
        searchEngine.index(DialogUids.getDialogUid(dialogItem.getType(), dialogItem.getId()),
                dialogItem.getSortKey(),
                dialogItem.getDialogTitle(),
                new GlobalSearch(dialogItem.getType(), dialogItem.getId(),
                        dialogItem.getDialogTitle(), dialogItem.getAvatar()));
    }

    private void removeIndex(long key) {
        searchEngine.remove(key);
    }

    private MessageDesc createDesc(MessageContent content) {
        if (content.getType() == 1) {
            try {
                im.actor.api.scheme.TextMessage textMessage = Bser.parse(im.actor.api.scheme.TextMessage.class,
                        content.getContent());
                return new MessageDesc(MessageType.TEXT, textMessage.getText(), false, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (content.getType() == 2) {
            try {
                ServiceMessage serviceMessage = Bser.parse(im.actor.api.scheme.ServiceMessage.class,
                        content.getContent());
                if (serviceMessage.getExtType() == 1) {
                    return new MessageDesc(MessageType.GROUP_USER_ADD, null, false, 0);
                } else if (serviceMessage.getExtType() == 2) {
                    return new MessageDesc(MessageType.GROUP_USER_KICK, null, false, 0);
                } else if (serviceMessage.getExtType() == 3) {
                    return new MessageDesc(MessageType.GROUP_USER_LEAVE, null, false, 0);
                } else if (serviceMessage.getExtType() == 4) {
                    return new MessageDesc(MessageType.GROUP_CREATED, null, false, 0);
                } else if (serviceMessage.getExtType() == 5) {
                    return new MessageDesc(MessageType.GROUP_TITLE, null, false, 0);
                } else if (serviceMessage.getExtType() == 6) {
                    return new MessageDesc(MessageType.GROUP_AVATAR, null, false, 0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (content.getType() == 3) {
            try {
                FileMessage fileMessage = Bser.parse(FileMessage.class, content.getContent());

                if (fileMessage.getExtType() == 0x01) {
                    return new MessageDesc(MessageType.PHOTO, null, false, 0);
                } else if (fileMessage.getExtType() == 0x02) {
                    return new MessageDesc(MessageType.VIDEO, null, false, 0);
                } else {
                    return new MessageDesc(MessageType.DOC, fileMessage.getName(), false, 0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new MessageDesc(MessageType.EMPTY, "", false, 0);
    }

    private MessageDesc createDesc(ConversationMessage conversationMessage) {
        if (conversationMessage.getContent() instanceof PhotoMessage) {
            return new MessageDesc(MessageType.PHOTO, null, false, 0);
        } else if (conversationMessage.getContent() instanceof VideoMessage) {
            return new MessageDesc(MessageType.VIDEO, null, false, 0);
        } else if (conversationMessage.getContent() instanceof TextMessage) {
            return new MessageDesc(MessageType.TEXT, ((TextMessage) conversationMessage.getContent()).getText(), false, 0);
        } else if (conversationMessage.getContent() instanceof DocumentMessage) {
            return new MessageDesc(MessageType.DOC, ((DocumentMessage) conversationMessage.getContent()).getName(), false, 0);
        } else if (conversationMessage.getContent() instanceof AudioMessage) {
            return new MessageDesc(MessageType.AUDIO, null, false, 0);
        } else if (conversationMessage.getContent() instanceof GroupKick) {
            int related = ((GroupKick) conversationMessage.getContent()).getKickedUid();
            return new MessageDesc(MessageType.GROUP_USER_KICK, null, false, related);
        } else if (conversationMessage.getContent() instanceof GroupAdd) {
            int related = ((GroupAdd) conversationMessage.getContent()).getAddedUid();
            return new MessageDesc(MessageType.GROUP_USER_ADD, null, false, related);
        } else if (conversationMessage.getContent() instanceof GroupLeave) {
            return new MessageDesc(MessageType.GROUP_USER_LEAVE, null, true, 0);
        } else if (conversationMessage.getContent() instanceof GroupCreated) {
            return new MessageDesc(MessageType.GROUP_CREATED, null, false, 0);
        } else if (conversationMessage.getContent() instanceof UserRegisteredMessage) {
            return new MessageDesc(MessageType.USER_REGISTERED, null, false, 0);
        } else if (conversationMessage.getContent() instanceof UserAddedDeviceMessage) {
            return new MessageDesc(MessageType.USER_ADDED_DEVICE, null, true, 0);
        } else if (conversationMessage.getContent() instanceof GroupTitle) {
            return new MessageDesc(MessageType.GROUP_TITLE, null, false, 0);
        } else if (conversationMessage.getContent() instanceof GroupAvatar) {
            if (((GroupAvatar) conversationMessage.getContent()).getNewAvatar() != null) {
                return new MessageDesc(MessageType.GROUP_AVATAR, null, false, 0);
            } else {
                return new MessageDesc(MessageType.GROUP_AVATAR_REMOVED, null, false, 0);
            }
        } else {
            throw new RuntimeException("Unknown message type: " + conversationMessage.getContent());
        }
    }

    private class MessageDesc {
        private int type;
        private String text;
        private boolean isSilent;
        private int relatedUid;

        private MessageDesc(int type, String text, boolean isSilent, int relatedUid) {
            this.type = type;
            this.text = text;
            this.isSilent = isSilent;
            this.relatedUid = relatedUid;
        }

        public int getType() {
            return type;
        }

        public String getText() {
            return text;
        }

        public boolean isSilent() {
            return isSilent;
        }

        public int getRelatedUid() {
            return relatedUid;
        }
    }
}