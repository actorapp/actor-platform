package im.actor.messenger.core.actors.messages;

import com.droidkit.actors.typed.TypedActor;
import com.droidkit.bser.Bser;

import im.actor.api.scheme.FileExPhoto;
import im.actor.api.scheme.FileExVideo;
import im.actor.api.scheme.MessageContent;
import im.actor.api.scheme.Peer;
import im.actor.api.scheme.PeerType;
import im.actor.api.scheme.encrypted.FileMessage;
import im.actor.api.scheme.encrypted.PhotoExtension;
import im.actor.api.scheme.encrypted.PlainFileLocation;
import im.actor.api.scheme.encrypted.PlainMessage;
import im.actor.api.scheme.encrypted.PlainPackage;
import im.actor.api.scheme.encrypted.VideoExtension;
import im.actor.crypto.DecryptException;
import im.actor.crypto.RsaDecryptCipher;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.core.actors.chat.ConversationActor;
import im.actor.messenger.core.actors.chat.OwnReadStateActor;
import im.actor.messenger.core.actors.chat.ReadStateActor;
import im.actor.messenger.core.actors.groups.GroupsActor;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.messages.FastThumb;
import im.actor.messenger.storage.scheme.messages.types.DocumentMessage;
import im.actor.messenger.storage.scheme.messages.types.PhotoMessage;
import im.actor.messenger.storage.scheme.messages.types.TextMessage;
import im.actor.messenger.storage.scheme.messages.types.VideoMessage;
import im.actor.messenger.util.BoxUtil;
import im.actor.messenger.util.Logger;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;

import static im.actor.messenger.core.Core.keyStorage;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class InMessagesActor extends TypedActor<InMessagesInt> implements InMessagesInt {

    private static TypedActorHolder<InMessagesInt> holder = new TypedActorHolder<InMessagesInt>(
            InMessagesInt.class, InMessagesActor.class, "updates", "receiver");

    public static InMessagesInt messageReceiver() {
        return holder.get();
    }

    private static final String TAG = "MessageReceiver";

    public InMessagesActor() {
        super(InMessagesInt.class);
    }

    @Override
    public void onEncryptedMessage(Peer peer, int senderUid, long date, byte[] messageKey, byte[] message) {

        if (peer.getType() == PeerType.PRIVATE) {
            UserModel convUser = users().get(peer.getId());
            if (convUser == null) {
                Logger.d(TAG, "Unable to find user: exit");
                return;
            }
        } else if (peer.getType() == PeerType.GROUP) {
            GroupModel group = groups().get(peer.getId());
            if (group == null) {
                Logger.d(TAG, "Unable to find user: group");
                return;
            }
        } else {
            return;
        }

        PrivateKey pk = keyStorage().getKeyPair().getPrivate();
        RsaDecryptCipher decryptCipher = new RsaDecryptCipher(pk);

        byte[] dest;
        try {
            dest = decryptCipher.decrypt(messageKey, message);
        } catch (DecryptException e) {
            e.printStackTrace();
            Logger.d(TAG, "Unable to decrypt message");
            return;
        }


        PlainMessage dm;

        try {
            PlainPackage dd = Bser.parse(PlainPackage.class, dest);
            if (dd.getMesssageType() == 1) {
                dm = Bser.parse(PlainMessage.class, dd.getBody());
            } else {
                Logger.d(TAG, "Unsupported package type #" + dd.getMesssageType());
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.d(TAG, "Unable to decrypt message");
            return;
        }

        Logger.d(TAG, "Loaded message");

        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();

        try {
            if (dm.getMessageTyoe() == 1) {
                TextMessage textMessage = Bser.parse(TextMessage.class, dm.getBody());

                ConversationActor.conv(chatType, chatId).onInMessage(dm.getGuid(),
                        senderUid, date,
                        new TextMessage(textMessage.getText(), textMessage.getText(), true));
            } else if (dm.getMessageTyoe() == 2) {
                FileMessage fileMessage = Bser.parse(FileMessage.class, dm.getBody());

                PlainFileLocation plainFileLocation = fileMessage.getFileLocation();

                FileLocation.Encryption encryptionType;
                switch (plainFileLocation.getEncryptionType()) {
                    case AES:
                        encryptionType = FileLocation.Encryption.AES;
                        break;
                    case AES_THEN_MAC:
                        encryptionType = FileLocation.Encryption.AES_THEN_MAC;
                        break;
                    default:
                    case NONE:
                        encryptionType = FileLocation.Encryption.NONE;
                        break;
                }

                FileLocation fileLocation = new FileLocation(plainFileLocation.getFileId(),
                        plainFileLocation.getAccessHash(),
                        plainFileLocation.getFileSize(), encryptionType, plainFileLocation.getEncryptedFileSize(),
                        plainFileLocation.getEncryptionKey());

                FastThumb fastThumb = null;
                if (fileMessage.getFastThumb() != null) {
                    fastThumb = new FastThumb(fileMessage.getFastThumb().getW(),
                            fileMessage.getFastThumb().getH(),
                            fileMessage.getFastThumb().getPreview());
                }

                if (fileMessage.getExtType() == 0x01) {
                    PhotoExtension extension = Bser.parse(PhotoExtension.class, fileMessage.getExtension());

                    ConversationActor.conv(chatType, chatId).onInMessage(
                            dm.getGuid(), senderUid, date,
                            new PhotoMessage(fileLocation,
                                    extension.getW(),
                                    extension.getH(),
                                    fastThumb, true));
                } else if (fileMessage.getExtType() == 0x02) {
                    VideoExtension extension = Bser.parse(VideoExtension.class, fileMessage.getExtension());

                    ConversationActor.conv(chatType, chatId).onInMessage(
                            dm.getGuid(), senderUid, date,
                            new VideoMessage(fileLocation, false, extension.getDuration(),
                                    extension.getW(), extension.getH(),
                                    fastThumb, true));
                } else {
                    ConversationActor.conv(chatType, chatId).onInMessage(
                            dm.getGuid(), senderUid, date,
                            new DocumentMessage(fileLocation, false, fileMessage.getName(),
                                    fileLocation.getFileSize(), true, fastThumb));
                }
            } else {
                return;
            }
        } catch (Exception e) {
            Logger.e(TAG, "", e);
            return;
        }

        system().actorOf(ReceivedEnctyptedActor.messageReceiver()).send(
                new ReceivedEnctyptedActor.Confirm(chatType, chatId, dm.getGuid()));
    }

    @Override
    public void onMessage(Peer peer, int senderUid, long date, long rid, MessageContent content) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();

        if (content.getType() == 1) {
            try {
                im.actor.api.scheme.TextMessage textMessage = Bser.parse(im.actor.api.scheme.TextMessage.class,
                        content.getContent());

                ConversationActor.conv(chatType, chatId).onInMessage(rid, senderUid, date,
                        new TextMessage(textMessage.getText(), textMessage.getText(), false));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else if (content.getType() == 3) {
            try {
                im.actor.api.scheme.FileMessage fileMessage = Bser.parse(im.actor.api.scheme.FileMessage.class,
                        content.getContent());
                FileLocation fileLocation = new FileLocation(fileMessage.getFileId(),
                        fileMessage.getAccessHash(),
                        fileMessage.getFileSize(), FileLocation.Encryption.NONE,
                        0, new byte[0]);
                FastThumb fastThumb = null;
                if (fileMessage.getThumb() != null) {
                    fastThumb = new FastThumb(fileMessage.getThumb().getW(),
                            fileMessage.getThumb().getH(),
                            fileMessage.getThumb().getThumb());
                }
                if (fileMessage.getExtType() == 0x01) {
                    FileExPhoto extension = Bser.parse(FileExPhoto.class,
                            fileMessage.getExt());

                    ConversationActor.conv(chatType, chatId).onInMessage(
                            rid, senderUid, date,
                            new PhotoMessage(fileLocation,
                                    extension.getW(),
                                    extension.getH(),
                                    fastThumb, false));
                } else if (fileMessage.getExtType() == 0x02) {
                    FileExVideo extension = Bser.parse(FileExVideo.class,
                            fileMessage.getExt());

                    ConversationActor.conv(chatType, chatId).onInMessage(
                            rid, senderUid, date,
                            new VideoMessage(fileLocation,
                                    false,
                                    extension.getDuration(),
                                    extension.getW(),
                                    extension.getH(),
                                    fastThumb, false));
                } else {

                    ConversationActor.conv(chatType, chatId).onInMessage(rid, senderUid, date,
                            new DocumentMessage(fileLocation, false, fileMessage.getName(),
                                    fileLocation.getFileSize(), false, fastThumb));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            return;
        }

        PlainReceivedActor.plainReceive().markReceived(chatType, chatId, date);
    }

    @Override
    public void onMessageSent(Peer peer, long rid, long date) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        long chatUid = DialogUids.getDialogUid(chatType, chatId);

        // Small hack for direct message loading
        MessageModel model = ListEngines.getMessagesListEngine(chatUid).getValue(rid);
        if (model != null) {
            ReadStateActor.readState().onNewOutMessage(chatType, chatId, date, rid,
                    model.getContent().isEncrypted());
        }
    }

    @Override
    public void onMessageReceived(Peer peer, long date) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        ReadStateActor.readState().markMessagesReceived(chatType, chatId, date);
    }

    @Override
    public void onMessageRead(Peer peer, long date) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        ReadStateActor.readState().markMessagesRead(chatType, chatId, date);
    }

    @Override
    public void onMessageReadByMe(Peer peer, long date) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        OwnReadStateActor.readState().messagePlainReadByMe(chatType, chatId, date);
    }

    @Override
    public void onMessageEncryptedReceived(Peer peer, long rid) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        ReadStateActor.readState().markEncryptedReceived(chatType, chatId, rid);
    }

    @Override
    public void onMessageEncryptedRead(Peer peer, long rid) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        ReadStateActor.readState().markEncryptedRead(chatType, chatId, rid);
    }

    @Override
    public void onMessageEncryptedReadByMe(Peer peer, long rid) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        OwnReadStateActor.readState().messageEncryptedReadByMe(chatType, chatId, rid);
    }

    @Override
    public void onMessageDeleted(Peer peer, List<Long> rid) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        ConversationActor.conv(chatType, chatId).deleteMessage(BoxUtil.unbox(rid.toArray(new Long[0])));
    }

    @Override
    public void onChatDelete(Peer peer) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        ConversationActor.conv(chatType, chatId).deleteChat();
    }

    @Override
    public void onChatClear(Peer peer) {
        int chatType = peer.getType() == PeerType.GROUP ? DialogType.TYPE_GROUP : DialogType.TYPE_USER;
        int chatId = peer.getId();
        ConversationActor.conv(chatType, chatId).clearChat();
    }
}