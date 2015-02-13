package im.actor.messenger.core.actors.chat;

import com.droidkit.actors.*;
import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedCreator;

import com.droidkit.bser.Bser;
import com.droidkit.engine.list.ListEngine;

import im.actor.api.scheme.FileExPhoto;
import im.actor.api.scheme.FileExVideo;
import im.actor.api.scheme.FileMessage;
import im.actor.api.scheme.HistoryMessage;
import im.actor.api.scheme.ServiceExChangedAvatar;
import im.actor.api.scheme.ServiceExChangedTitle;
import im.actor.api.scheme.ServiceExUserAdded;
import im.actor.api.scheme.ServiceExUserKicked;
import im.actor.api.scheme.ServiceMessage;
import im.actor.messenger.api.ApiConversion;
import im.actor.messenger.core.actors.notifications.NotificationsActor;
import im.actor.messenger.core.actors.send.MediaSenderActor;
import im.actor.messenger.core.actors.send.MessageDeliveryActor;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.storage.DialogStorage;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.MessageType;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.media.Document;
import im.actor.messenger.storage.scheme.messages.*;
import im.actor.messenger.storage.scheme.messages.MessageState;
import im.actor.messenger.storage.scheme.messages.types.*;
import im.actor.messenger.util.RandomUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.groups;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class ConversationActor extends TypedActor<ConversationInt> implements ConversationInt {

    private static final HashMap<Long, ConversationInt> PROXIES = new HashMap<Long, ConversationInt>();

    public static ConversationInt conv(final int type, final int id) {
        long uid = DialogUids.getDialogUid(type, id);
        synchronized (PROXIES) {
            if (!PROXIES.containsKey(uid)) {
                ConversationInt res = TypedCreator.typed(ActorSystem.system().actorOf(selection(type, id)), ConversationInt.class);
                PROXIES.put(uid, res);
            }
            return PROXIES.get(uid);
        }
    }

    private static ActorSelection selection(final int type, final int id) {
        return new ActorSelection(Props.create(ConversationActor.class, new ActorCreator<ConversationActor>() {
            @Override
            public ConversationActor create() {
                return new ConversationActor(type, id);
            }
        }), "conv_" + type + "_" + id);
    }

    private int type;
    private int id;
    private long uid;
    private ListEngine<MessageModel> listEngine;
    private ListEngine<Document> documentsEngine;
    private DialogsInt dialogsActor;

    public ConversationActor(int type, int id) {
        super(ConversationInt.class);
        this.type = type;
        this.id = id;
        this.uid = DialogUids.getDialogUid(type, id);
    }

    @Override
    public void preStart() {
        this.listEngine = ListEngines.getMessagesListEngine(uid);
        this.documentsEngine = ListEngines.getDocuments(uid);
        this.dialogsActor = DialogsActor.dialogs();
    }

    private void onNewMessage() {

    }

    private ConversationMessage createMessage(long rid, long date, MessageState messageState,
                                              int senderId, AbsMessage content) {
        // TODO: better sorting keys
        long sortKey = System.currentTimeMillis();
        return new ConversationMessage(rid, sortKey, date, senderId, messageState, content);
    }

    @Override
    public void onInMessage(long rid, int senderId, long date, AbsMessage content) {
        if (listEngine.getValue(rid) != null) {
            return;
        }

        ConversationMessage message = createMessage(rid, date, MessageState.SENT, senderId, content);

        listEngine.addOrUpdateItem(new MessageModel(message));

        dialogsActor.onMessageArrived(type, id, message);

        if (senderId != myUid()) {
            OwnReadStateActor.readState().newMessage(type, id, rid, message.getSortKey(),
                    message.getTime(), message.getContent().isEncrypted());
            if ((!(content instanceof GroupLeave)) && (!(content instanceof UserAddedDeviceMessage))) {
                NotificationsActor.notifications().onNewMessage(type, id, message);
            }
            onNewMessage();
        } else {
            OwnReadStateActor.readState().newOutMessage(type, id, rid, message.getSortKey(),
                    message.getTime(), message.getContent().isEncrypted());
            ReadStateActor.readState().onNewOutMessage(type, id, message.getTime(), message.getRid(),
                    content.isEncrypted());
        }

        if (content instanceof DocumentMessage) {
            DocumentMessage documentMessage = (DocumentMessage) content;
            documentsEngine.addOrUpdateItem(new Document(rid, documentMessage.getLocation(), documentMessage.getName(),
                    "", senderId, date));
        }
    }

    @Override
    public void onOutText(long rid, String text, boolean isEncrypted) {

        ConversationMessage message = createMessage(rid, System.currentTimeMillis(), MessageState.PENDING, myUid(),
                new TextMessage(text, text, isEncrypted));

        listEngine.addOrUpdateItem(new MessageModel(message));

        dialogsActor.onMessageArrived(type, id, message);

        OwnReadStateActor.readState().newOutMessage(type, id, rid, message.getSortKey(),
                message.getTime(), message.getContent().isEncrypted());
    }

    @Override
    public Future<MessageModel> onStartUpload(AbsFileMessage content) {
        long rid = RandomUtil.randomId();
        ConversationMessage message = createMessage(rid, System.currentTimeMillis(), MessageState.PENDING, myUid(), content);

        listEngine.addOrUpdateItem(new MessageModel(message));

        dialogsActor.onMessageArrived(type, id, message);

        OwnReadStateActor.readState().newOutMessage(type, id, rid, message.getSortKey(),
                message.getTime(), message.getContent().isEncrypted());

        return result(listEngine.getValue(rid));
    }

    @Override
    public void onUploaded(long rid, FileLocation fileLocation) {
        MessageModel message = listEngine.getValue(rid);
        if (message == null) {
            return;
        }
        if (!(message.getRaw().getContent() instanceof AbsFileMessage)) {
            return;
        }

        AbsFileMessage uploadingMessage = (AbsFileMessage) message.getRaw().getContent();

        update(message, message.getRaw().changeContent(uploadingMessage.uploaded(fileLocation)));
    }

    @Override
    public void onMessageTryAgain(long rid) {
        MessageModel message = listEngine.getValue(rid);
        if (message != null) {
            update(message, message.getRaw().changeState(MessageState.PENDING));

            dialogsActor.onMessageStateChanged(type, id, rid, MessageState.PENDING);
        }
    }

    @Override
    public void onMessageSent(long rid, long date) {
        MessageModel message = listEngine.getValue(rid);
        if (message != null) {
            update(message, message.getRaw()
                    .changeState(MessageState.SENT)
                    .changeDate(date));

            if (message.getRaw().getContent() instanceof DocumentMessage) {
                DocumentMessage documentMessage = (DocumentMessage) message.getRaw().getContent();
                documentsEngine.addOrUpdateItem(new Document(rid, documentMessage.getLocation(), documentMessage.getName(),
                        "", message.getRaw().getSenderId(), message.getRaw().getTime()));
            }

            dialogsActor.onMessageStateChanged(type, id, rid, MessageState.SENT);
        }
    }

    @Override
    public void onMessageMarkReceived(long rid) {
        MessageModel message = listEngine.getValue(rid);
        if (message != null) {
            if (message.getRaw().getMessageState() == MessageState.READ) {
                return;
            }
            update(message, message.getRaw().changeState(MessageState.RECEIVED));

            dialogsActor.onMessageStateChanged(type, id, rid, MessageState.RECEIVED);
        }
    }

    @Override
    public void onMessageMarkRead(long rid) {
        MessageModel message = listEngine.getValue(rid);
        if (message != null) {
            update(message, message.getRaw().changeState(MessageState.READ));

            dialogsActor.onMessageStateChanged(type, id, rid, MessageState.READ);
        }
    }

    @Override
    public void onMessageError(long rid) {
        MessageModel message = listEngine.getValue(rid);
        if (message != null) {
            update(message, message.getRaw().changeState(MessageState.ERROR));

            dialogsActor.onMessageStateChanged(type, id, rid, MessageState.ERROR);
        }
    }

    @Override
    public void onMessageDownloaded(long rid) {
        MessageModel message = listEngine.getValue(rid);
        if (message == null) {
            return;
        }

        if (message.getRaw().getContent() instanceof AbsFileMessage) {
            update(message, message.getRaw().changeContent(((AbsFileMessage) message.getRaw().getContent()).downloaded()));
        }
    }

    @Override
    public void onMessageUnDownloaded(long rid) {
        MessageModel message = listEngine.getValue(rid);
        if (message.getRaw().getContent() instanceof AbsFileMessage) {
            update(message, message.getRaw().changeContent(((AbsFileMessage) message.getRaw().getContent()).undownloaded()));
        }
    }

    private void update(MessageModel model, ConversationMessage msg) {
        model.update(msg);
        listEngine.addOrUpdateItem(model);
    }

    @Override
    public void clearChat() {
        listEngine.clear();
        documentsEngine.clear();
        DialogsActor.dialogs().onChatClear(type, id);
        MessageDeliveryActor.messageSender().mediaCancelAll(type, id);
    }

    @Override
    public void deleteChat() {
        listEngine.clear();
        documentsEngine.clear();
        DialogsActor.dialogs().onDialogDelete(type, id);
        MessageDeliveryActor.messageSender().mediaCancelAll(type, id);
    }

    @Override
    public void deleteMessage(long[] rids) {
        listEngine.removeItems(rids);
        MessageModel message = listEngine.getHeadValue();
        DialogsActor.dialogs().onDeleteMessages(type, id, rids, message != null ? message.getRaw() : null);

        for (long l : rids) {
            MessageDeliveryActor.messageSender().mediaCancel(type, id, l);
            documentsEngine.removeItem(l);
        }
    }

    @Override
    public void onHistoryLoaded(List<HistoryMessage> history) {
        ArrayList<MessageModel> updated = new ArrayList<MessageModel>();
        for (HistoryMessage message : history) {
            MessageModel model = listEngine.getValue(message.getRid());
            if (model == null) {
                AbsMessage content = null;
                if (message.getMessage().getType() == 1) {
                    try {
                        im.actor.api.scheme.TextMessage textMessage = Bser.parse(im.actor.api.scheme.TextMessage.class,
                                message.getMessage().getContent());

                        content = new TextMessage(textMessage.getText(), textMessage.getText(),
                                false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (message.getMessage().getType() == 2) {
                    try {
                        ServiceMessage serviceMessage = Bser.parse(im.actor.api.scheme.ServiceMessage.class,
                                message.getMessage().getContent());
                        if (serviceMessage.getExtType() == 1) {
                            ServiceExUserAdded added = Bser.parse(ServiceExUserAdded.class, serviceMessage.getExt());
                            content = new GroupAdd(added.getAddedUid());
                        } else if (serviceMessage.getExtType() == 2) {
                            ServiceExUserKicked kicked = Bser.parse(ServiceExUserKicked.class, serviceMessage.getExt());
                            content = new GroupKick(kicked.getKickedUid());
                        } else if (serviceMessage.getExtType() == 3) {
                            content = new GroupLeave();
                        } else if (serviceMessage.getExtType() == 4) {
                            content = new GroupCreated("??");
                        } else if (serviceMessage.getExtType() == 5) {
                            ServiceExChangedTitle title = Bser.parse(ServiceExChangedTitle.class, serviceMessage.getExt());
                            content = new GroupTitle(title.getTitle());
                        } else if (serviceMessage.getExtType() == 6) {
                            ServiceExChangedAvatar avatar = Bser.parse(ServiceExChangedAvatar.class, serviceMessage.getExt());
                            content = new GroupAvatar(ApiConversion.convert(avatar.getAvatar()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (message.getMessage().getType() == 3) {
                    try {
                        FileMessage fileMessage = Bser.parse(FileMessage.class, message.getMessage().getContent());

                        if (fileMessage.getExtType() == 0x01) {
                            FileExPhoto photo = Bser.parse(FileExPhoto.class, fileMessage.getExt());
                            content = new PhotoMessage(new FileLocation(fileMessage.getFileId(),
                                    fileMessage.getAccessHash(), fileMessage.getFileSize()), false,
                                    photo.getW(), photo.getH(), new FastThumb(
                                    fileMessage.getThumb().getW(),
                                    fileMessage.getThumb().getH(),
                                    fileMessage.getThumb().getThumb()), false);
                        } else if (fileMessage.getExtType() == 0x02) {
                            FileExVideo video = Bser.parse(FileExVideo.class, fileMessage.getExt());
                            FastThumb fastThumb = null;
                            if (fileMessage.getThumb() != null) {
                                fastThumb = new FastThumb(fileMessage.getThumb().getW(),
                                        fileMessage.getThumb().getH(),
                                        fileMessage.getThumb().getThumb());
                            }
                            content = new VideoMessage(new FileLocation(fileMessage.getFileId(),
                                    fileMessage.getAccessHash(), fileMessage.getFileSize()), false,
                                    video.getDuration(), video.getW(), video.getH(), fastThumb, false);
                        } else {
                            content = new DocumentMessage(new FileLocation(fileMessage.getFileId(),
                                    fileMessage.getAccessHash(), fileMessage.getFileSize()), false, fileMessage.getName(),
                                    fileMessage.getFileSize(), false, null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (content != null) {
                    updated.add(new MessageModel(
                            new ConversationMessage(message.getRid(), message.getDate(),
                                    message.getDate(), message.getSenderUid(), MessageState.SENT,
                                    content)));
                }
            }
        }
        listEngine.addOrUpdateItems(updated);
    }
}
