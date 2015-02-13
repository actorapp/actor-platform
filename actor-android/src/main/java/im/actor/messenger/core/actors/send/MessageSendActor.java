package im.actor.messenger.core.actors.send;

import android.util.Log;

import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.tasks.AskCallback;
import com.droidkit.actors.tasks.AskFuture;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.bser.Bser;
import com.droidkit.engine.persistence.BserMap;
import com.droidkit.engine.persistence.PersistenceMap;
import com.droidkit.engine.persistence.storage.SqliteStorage;

import im.actor.api.ApiRequestException;
import im.actor.api.scheme.EncryptedAesKey;
import im.actor.api.scheme.FastThumb;
import im.actor.api.scheme.FileMessage;
import im.actor.api.scheme.MessageContent;
import im.actor.api.scheme.OutPeer;
import im.actor.api.scheme.Peer;
import im.actor.api.scheme.PeerType;
import im.actor.api.scheme.TextMessage;
import im.actor.api.scheme.WrongKeysErrorData;
import im.actor.api.scheme.rpc.ResponseSeqDate;
import im.actor.api.scheme.updates.UpdateMessageSent;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.core.actors.chat.ConversationActor;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.core.actors.encryption.RsaActor;
import im.actor.messenger.core.actors.encryption.RsaResult;
import im.actor.messenger.core.actors.keys.KeyLoaderActor;
import im.actor.messenger.core.actors.users.UserActor;
import im.actor.messenger.core.encryption.AESEncryptionUtils;
import im.actor.messenger.core.encryption.CryptoUtils;
import im.actor.messenger.core.encryption.PublicKeysStorage;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.DbProvider;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.messages.PendingMessage;
import im.actor.messenger.storage.scheme.users.PublicKey;
import im.actor.messenger.util.Logger;
import im.actor.messenger.util.RandomUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static im.actor.messenger.core.Core.keyStorage;
import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.util.io.StreamingUtils.intToBytes;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class MessageSendActor extends TypedActor<MessageSendInt> implements MessageSendInt {

    private static TypedActorHolder<MessageSendInt> HOLDER = new TypedActorHolder<MessageSendInt>(
            MessageSendInt.class, MessageSendActor.class, "sender");

    public static MessageSendInt messageSender() {
        return HOLDER.get();
    }

    private static final String TAG = "SendActor";

    public MessageSendActor() {
        super(MessageSendInt.class);
    }

    private PersistenceMap<PendingMessage> pending;

    @Override
    public void preStart() {
        super.preStart();
        pending = new BserMap<PendingMessage>(new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()), "messages"), PendingMessage.class);
        for (PendingMessage p : pending.values()) {
            self().send(p);
        }
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof PendingMessage) {
            PendingMessage msg = (PendingMessage) message;
            if (msg.isEncrypted()) {
                sendEncryptedMessage(msg);
            } else {
                sendPlainMessage(msg);
            }
        }
    }

    @Override
    public void sendText(int chatType, int chatId, String text, boolean isEncrypted) {
        long randomId = RandomUtil.randomId();
        ConversationActor.conv(chatType, chatId).onOutText(randomId, text, isEncrypted);
        if (isEncrypted) {
            byte[] message = EncryptedMessages.createTextMessage(randomId, text);
            PendingMessage pendingMessage = new PendingMessage(randomId, chatType, chatId, message, 0, true);
            pending.put(randomId, pendingMessage);
            sendEncryptedMessage(pendingMessage);
        } else {
            PendingMessage pendingMessage = new PendingMessage(randomId, chatType, chatId,
                    new TextMessage(text, 0, null).toByteArray(), 1, false);
            pending.put(randomId, pendingMessage);
            sendPlainMessage(pendingMessage);
        }
    }

    @Override
    public void sendFile(int chatType, int chatId, long rid, FileLocation fileLocation,
                         String name, int extType, byte[] extension, byte[] thumb,
                         boolean isEncrypted) {
        ConversationActor.conv(chatType, chatId).onUploaded(rid, fileLocation);
        if (isEncrypted) {
            byte[] message = EncryptedMessages.createFileMessage(rid, fileLocation,
                    name, extType, extension, thumb);
            PendingMessage pendingMessage = new PendingMessage(rid, chatType, chatId, message, 0, true);
            pending.put(rid, pendingMessage);
            sendEncryptedMessage(pendingMessage);
        } else {
            FastThumb fastThumb = null;
            if (thumb != null && thumb.length > 0) {
                try {
                    fastThumb = Bser.parse(FastThumb.class, thumb);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String mimeType;

            if (extType == 0x01) {
                mimeType = "image/jpeg";
            } else if (extType == 0x02) {
                mimeType = "video/mp4";
            } else if (extType == 0x03) {
                mimeType = "audio/ogg";
            } else {
                mimeType = "application/octet-stream";
            }

            FileMessage fileMessage = new FileMessage(fileLocation.getFileId(),
                    fileLocation.getAccessHash(),
                    fileLocation.getFileSize(), name, mimeType, fastThumb, extType, extension);

            PendingMessage pendingMessage = new PendingMessage(rid, chatType, chatId,
                    fileMessage.toByteArray(), 3, false);
            pending.put(rid, pendingMessage);
            sendPlainMessage(pendingMessage);
        }
    }

    private void sendPlainMessage(final PendingMessage message) {

        final long randomId = message.getRid();
        final int chatType;
        final int chatId;
        final OutPeer outPeer;
        final Peer peer;

        if (message.getConvType() == DialogType.TYPE_USER) {
            chatType = DialogType.TYPE_USER;
            chatId = message.getConvId();
            UserModel receiver = users().get(chatId);
            if (receiver == null) {
                pending.remove(message.getRid());
                return;
            }
            outPeer = new OutPeer(PeerType.PRIVATE, chatId, receiver.getAccessHash());
            peer = new Peer(PeerType.PRIVATE, chatId);
        } else if (message.getConvType() == DialogType.TYPE_GROUP) {
            chatType = DialogType.TYPE_GROUP;
            chatId = message.getConvId();
            GroupModel group = groups().get(chatId);
            if (group == null) {
                pending.remove(message.getRid());
                return;
            }
            outPeer = new OutPeer(PeerType.GROUP, chatId, group.getAccessHash());
            peer = new Peer(PeerType.GROUP, chatId);
        } else {
            return;
        }

        ask(requests().sendMessage(outPeer, randomId,
                        new MessageContent(message.getMessageType(), message.getMessageContent())),
                new FutureCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate result) {
                        Log.d(TAG, "Sent #" + randomId);
                        pending.remove(randomId);

                        ConversationActor.conv(chatType, chatId).onMessageSent(randomId, result.getDate());
                        system().actorOf(SequenceActor.sequence()).send(new SequenceActor.SeqUpdate(result.getSeq(), result.getState(),
                                new UpdateMessageSent(peer, randomId, result.getDate())));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        pending.remove(randomId);
                        ConversationActor.conv(chatType, chatId).onMessageError(randomId);
                    }
                });
    }

    private void sendEncryptedMessage(final PendingMessage message) {

        final int uid;
        if (message.getConvType() == DialogType.TYPE_USER) {
            uid = message.getConvId();
        } else {
            // TODO: Implement group sending
            return;
        }

        long keysStart = System.currentTimeMillis();

        UserModel receiver = users().get(uid);
        UserModel me = users().get(myUid());

        if (receiver == null || me == null) {
            pending.remove(message.getRid());
            return;
        }

        java.security.PublicKey pk = keyStorage().getKeyPair().getPublic();
        long myKey = CryptoUtils.keyHash(pk);

        PublicKeysStorage.KeyLoadResult myKeys = PublicKeysStorage.getUserPublicKeys(me.getRaw(), myKey);
        PublicKeysStorage.KeyLoadResult userKeys = PublicKeysStorage.getUserPublicKeys(receiver.getRaw(), 0);

        if (myKeys.getMissing().size() > 0 || userKeys.getMissing().size() > 0) {
            Logger.d(TAG, "We need some keys, loading " + (myKeys.getMissing().size() + userKeys.getMissing().size()) + " required keys");
            List<AskFuture> list = new ArrayList<AskFuture>();
            for (Long m : myKeys.getMissing()) {
                list.add(ask(KeyLoaderActor.loader(me.getId(), me.getAccessHash(), m)));
            }
            for (Long m : userKeys.getMissing()) {
                list.add(ask(KeyLoaderActor.loader(receiver.getId(), receiver.getAccessHash(), m)));
            }
            combine(new AskCallback<Object[]>() {
                @Override
                public void onResult(Object[] result) {
                    Log.d(TAG, "Sending message after key loading");
                    sendEncryptedMessage(message);
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.d(TAG, "Key load error" + throwable);
                }
            }, list.toArray(new AskFuture[0]));
            return;
        }
        Logger.d(TAG, "Loading keys in " + (System.currentTimeMillis() - keysStart) + " ms");

        long aesStart = System.currentTimeMillis();

        byte[] aesKey = CryptoUtils.generateSeed(32);
        byte[] aesIv = CryptoUtils.generateSeed(16);
        byte[] key = CryptoUtils.concat(aesKey, aesIv);
        byte[] plainMessage = CryptoUtils.concat(intToBytes(message.getMessageContent().length),
                message.getMessageContent());

        final byte[] encrypted = AESEncryptionUtils.encrypt(plainMessage, aesKey, aesIv);
        Logger.d(TAG, "AES in " + (System.currentTimeMillis() - aesStart) + " ms");

        ask(RsaActor.encryptor().encrypt(key, myKeys.getKeys().toArray(new PublicKey[0]),
                userKeys.getKeys().toArray(new PublicKey[0])), new FutureCallback<RsaResult>() {
            @Override
            public void onResult(RsaResult result) {
                Log.d(TAG, "Successfuly encrypted");

                List<EncryptedAesKey> ownKeys = new ArrayList<EncryptedAesKey>();
                List<EncryptedAesKey> keys = new ArrayList<EncryptedAesKey>();

                for (RsaResult.RsaPart my : result.getMyParts()) {
                    ownKeys.add(new EncryptedAesKey(my.getKeyHash(), my.getEncryptedData()));
                }

                for (RsaResult.RsaPart my : result.getForeignParts()) {
                    keys.add(new EncryptedAesKey(my.getKeyHash(), my.getEncryptedData()));
                }

                sendEncryptedMessage(message, keys, ownKeys, encrypted);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "Error #" + message.getRid() + " error " + throwable);
                pending.remove(message.getRid());
                ConversationActor.conv(0, uid).onMessageError(message.getRid());
            }
        });
    }

    private void sendEncryptedMessage(final PendingMessage message, List<EncryptedAesKey> keys,
                                      List<EncryptedAesKey> ownKeys, byte[] encryptedMessage) {

        final long randomId = message.getRid();
        final int uid = message.getConvId();
        UserModel receiver = users().get(uid);

        ask(requests().sendEncryptedMessage(new OutPeer(PeerType.PRIVATE, uid, receiver.getAccessHash()),
                message.getRid(), encryptedMessage, keys, ownKeys), new FutureCallback<ResponseSeqDate>() {
            @Override
            public void onResult(ResponseSeqDate result) {
                Log.d(TAG, "Sent #" + randomId);
                pending.remove(randomId);

                ConversationActor.conv(0, uid).onMessageSent(randomId, result.getDate());
                system().actorOf(SequenceActor.sequence()).send(new SequenceActor.SeqUpdate(result.getSeq(), result.getState(),
                        new UpdateMessageSent(new Peer(PeerType.PRIVATE, uid), randomId, result.getDate())));
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "Sent #" + randomId + " error" + throwable);
                if (throwable instanceof ApiRequestException) {
                    ApiRequestException apiRequestException = (ApiRequestException) throwable;
                    if (apiRequestException.getErrorTag().equals("WRONG_KEYS")) {
                        try {
                            WrongKeysErrorData data = Bser.parse(WrongKeysErrorData.class, apiRequestException.getRelatedData());
                            UserActor.userActor().onWrongKeys(data.getNewKeys(), data.getInvalidKeys(), data.getRemovedKeys());
                            sendEncryptedMessage(message);
                            Log.d(TAG, "Trying resend #" + randomId);
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                pending.remove(randomId);
                ConversationActor.conv(0, uid).onMessageError(randomId);
            }
        });
    }
}