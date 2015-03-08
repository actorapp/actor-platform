package im.actor.model.modules.updates;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.annotation.Verified;
import im.actor.model.api.EncryptedDocumentV1;
import im.actor.model.api.EncryptedDocumentV1ExPhoto;
import im.actor.model.api.EncryptedDocumentV1VExideo;
import im.actor.model.api.EncryptedMessageV1;
import im.actor.model.api.EncryptedPackage;
import im.actor.model.api.EncryptedTextContentV1;
import im.actor.model.api.HistoryMessage;
import im.actor.model.api.rpc.ResponseLoadDialogs;
import im.actor.model.api.rpc.ResponseLoadHistory;
import im.actor.model.crypto.AesCipher;
import im.actor.model.crypto.RsaCipher;
import im.actor.model.droidkit.bser.Bser;
import im.actor.model.entity.ContentDescription;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FastThumb;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.ServiceUserRegistered;
import im.actor.model.entity.content.TextContent;
import im.actor.model.entity.content.VideoContent;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.ConversationActor;
import im.actor.model.modules.messages.ConversationHistoryActor;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.DialogsHistoryActor;
import im.actor.model.modules.messages.OwnReadActor;
import im.actor.model.modules.messages.PlainReceiverActor;
import im.actor.model.modules.messages.SenderActor;
import im.actor.model.modules.messages.entity.DialogHistory;
import im.actor.model.modules.messages.entity.EntityConverter;
import im.actor.model.modules.utils.RandomUtils;

import static im.actor.model.modules.messages.entity.EntityConverter.convert;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class MessagesProcessor extends BaseModule {
    public MessagesProcessor(Modules messenger) {
        super(messenger);
    }

    @Verified
    public void onDialogsLoaded(ResponseLoadDialogs dialogsResponse) {

        // Should we eliminate DialogHistory?

        ArrayList<DialogHistory> dialogs = new ArrayList<DialogHistory>();

        long maxLoadedDate = Long.MAX_VALUE;

        for (im.actor.model.api.Dialog dialog : dialogsResponse.getDialogs()) {

            maxLoadedDate = Math.min(dialog.getSortDate(), maxLoadedDate);

            Peer peer = convert(dialog.getPeer());
            AbsContent msgContent = convert(dialog.getMessage());

            if (msgContent == null) {
                continue;
            }

            dialogs.add(new DialogHistory(peer, dialog.getUnreadCount(), dialog.getSortDate(),
                    dialog.getRid(), dialog.getDate(), dialog.getSenderUid(), msgContent, convert(dialog.getState())));
        }

        // Sending updates to dialogs actor
        if (dialogs.size() > 0) {
            dialogsActor().send(new DialogsActor.HistoryLoaded(dialogs));
        }

        // Sending notification to history actor
        dialogsHistoryActor().send(new DialogsHistoryActor.LoadedMore(dialogsResponse.getDialogs().size(),
                maxLoadedDate));
    }

    @Verified
    public void onMessagesLoaded(Peer peer, ResponseLoadHistory historyResponse) {
        ArrayList<Message> messages = new ArrayList<Message>();
        long maxLoadedDate = Long.MAX_VALUE;
        for (HistoryMessage historyMessage : historyResponse.getHistory()) {

            maxLoadedDate = Math.min(historyMessage.getDate(), maxLoadedDate);

            AbsContent content = EntityConverter.convert(historyMessage.getMessage());
            if (content == null) {
                continue;
            }
            MessageState state = EntityConverter.convert(historyMessage.getState());

            messages.add(new Message(historyMessage.getRid(), historyMessage.getDate(),
                    historyMessage.getDate(), historyMessage.getSenderUid(),
                    state, content));
        }

        // Sending updates to conversation actor
        if (messages.size() > 0) {
            conversationActor(peer).send(new ConversationActor.HistoryLoaded(messages));
        }

        // Sending notification to conversation history actor
        conversationHistoryActor(peer).send(new ConversationHistoryActor.LoadedMore(historyResponse.getHistory().size(),
                maxLoadedDate));
    }


    public void onMessage(im.actor.model.api.Peer _peer, int senderUid, long date, long rid,
                          im.actor.model.api.Message content) {

        Peer peer = convert(_peer);
        AbsContent msgContent = convert(content);
        if (msgContent == null) {
            // Ignore if content is unsupported
            return;
        }

        onMessage(peer, senderUid, date, rid, msgContent);
    }

    public void onEncryptedMessage(im.actor.model.api.Peer _peer, int senderUid, long date,
                                   long keyHash, byte[] aesEncryptedKey, byte[] message) {
        Peer peer = convert(_peer);

        RsaCipher cipher = crypto().createRSAOAEPSHA1Cipher(modules().getAuthModule().getPublicKey(),
                modules().getAuthModule().getPrivateKey());
        byte[] aesKey = cipher.decrypt(aesEncryptedKey);
        if (aesKey == null) {
            // unable to decrypt message
            return;
        }

        byte[] key = substring(aesKey, aesKey.length - 16 - 32, 32);
        byte[] iv = substring(aesKey, aesKey.length - 16, 16);

        AesCipher aesCipher = crypto().createAESCBCPKS7Cipher(key, iv);

        try {
            byte[] decryptedRawMessage = aesCipher.decrypt(message);

            int len = readInt(decryptedRawMessage, 0);
            byte[] res = substring(decryptedRawMessage, 4, len);

            EncryptedPackage encryptedPackage = Bser.parse(new EncryptedPackage(), res);
            if (encryptedPackage.getV2Message() != null) {
                // Process V2
            } else {
                if (encryptedPackage.getV1MessageType() != 1) {
                    return;
                }

                EncryptedMessageV1 messageV1 = Bser.parse(new EncryptedMessageV1(), encryptedPackage.getV1Message());
                if (messageV1.getContent() instanceof EncryptedTextContentV1) {
                    EncryptedTextContentV1 text = (EncryptedTextContentV1) messageV1.getContent();
                    onMessage(peer, senderUid, date, messageV1.getRid(), new TextContent(text.getText()));
                } else if (messageV1.getContent() instanceof EncryptedDocumentV1) {
                    EncryptedDocumentV1 document = (EncryptedDocumentV1) messageV1.getContent();
                    AbsContent content;
                    String name = document.getName();
                    String mimeType = document.getMimeType();
                    FastThumb fastThumb = EntityConverter.convert(document.getFastThumb());
                    FileRemoteSource fileRemoteSource = new FileRemoteSource(new FileReference(
                            document.getFileLocation().getFileId(),
                            document.getFileLocation().getAccessHash(),
                            document.getFileLocation().getFileSize(),
                            document.getName()));

                    if (document.getExtension() instanceof EncryptedDocumentV1ExPhoto) {
                        EncryptedDocumentV1ExPhoto photo = (EncryptedDocumentV1ExPhoto) document.getExtension();
                        content = new PhotoContent(fileRemoteSource, mimeType, name, fastThumb,
                                photo.getWidth(), photo.getHeight());
                    } else if (document.getExtension() instanceof EncryptedDocumentV1VExideo) {
                        EncryptedDocumentV1VExideo video = (EncryptedDocumentV1VExideo) document.getExtension();
                        content = new VideoContent(fileRemoteSource, mimeType, name, fastThumb,
                                video.getDuration(), video.getWidth(), video.getHeight());
                    } else {
                        content = new DocumentContent(fileRemoteSource, mimeType, name, fastThumb);
                    }
                    onMessage(peer, senderUid, date, messageV1.getRid(), content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onMessage(Peer peer, int senderUid, long date, long rid, AbsContent msgContent) {
        boolean isOut = myUid() == senderUid;

        // Sending message to conversation
        Message message = new Message(rid, date, date, senderUid,
                isOut ? MessageState.SENT : MessageState.UNKNOWN, msgContent);
        conversationActor(peer).send(message);

        if (!isOut) {

            // Send to OwnReadActor for adding to unread index
            ownReadActor().send(new OwnReadActor.NewMessage(peer, rid, date, false));

            // Notify notification actor
            modules().getNotifications().onInMessage(peer, senderUid, date,
                    ContentDescription.fromContent(message.getContent()));

            // mark message as received
            plainReceiveActor().send(new PlainReceiverActor.MarkReceived(peer, date));

        } else {

            // Send information to OwnReadActor about out message
            ownReadActor().send(new OwnReadActor.NewOutMessage(peer, rid, date, false));
        }
    }

    public static int readInt(byte[] bytes, int offset) {
        int a = bytes[offset] & 0xFF;
        int b = bytes[offset + 1] & 0xFF;
        int c = bytes[offset + 2] & 0xFF;
        int d = bytes[offset + 3] & 0xFF;

        return d + (c << 8) + (b << 16) + (a << 24);
    }

    public static byte[] substring(byte[] src, int start, int len) {
        byte[] res = new byte[len];
        System.arraycopy(src, start, res, 0, len);
        return res;
    }

    @Verified
    public void onMessageRead(im.actor.model.api.Peer _peer, long startDate, long readDate) {
        Peer peer = convert(_peer);

        // Sending event to conversation actor
        conversationActor(peer).send(new ConversationActor.MessageRead(startDate));
    }

    @Verified
    public void onMessageEncryptedRead(im.actor.model.api.Peer _peer, long rid, long readDate) {
        Peer peer = convert(_peer);

        // Sending event to conversation actor
        conversationActor(peer).send(new ConversationActor.MessageEncryptedRead(rid));
    }

    @Verified
    public void onMessageReceived(im.actor.model.api.Peer _peer, long startDate, long receivedDate) {
        Peer peer = convert(_peer);

        // Sending event to conversation actor
        conversationActor(peer).send(new ConversationActor.MessageReceived(startDate));
    }

    @Verified
    public void onMessageEncryptedReceived(im.actor.model.api.Peer _peer, long rid, long receivedDate) {
        Peer peer = convert(_peer);

        // Sending event to conversation actor
        conversationActor(peer).send(new ConversationActor.MessageEncryptedReceived(rid));
    }

    @Verified
    public void onMessageReadByMe(im.actor.model.api.Peer _peer, long startDate) {
        Peer peer = convert(_peer);

        // Sending event to OwnReadActor for syncing read state across devices
        ownReadActor().send(new OwnReadActor.MessageReadByMe(peer, startDate));
    }

    @Verified
    public void onMessageEncryptedReadByMe(im.actor.model.api.Peer _peer, long rid) {
        Peer peer = convert(_peer);

        // Sending event to OwnReadActor for syncing read state across devices
        ownReadActor().send(new OwnReadActor.MessageReadByMeEncrypted(peer, rid));
    }

    public void onMessageDelete(im.actor.model.api.Peer _peer, List<Long> rids) {
        Peer peer = convert(_peer);

        // Deleting messages from conversation
        conversationActor(peer).send(new ConversationActor.MessagesDeleted(rids));

        // Remove messages from unread index
        ownReadActor().send(new OwnReadActor.MessageDeleted(peer, rids));

        // TODO: Notify send actor for canceling
    }

    public void onMessageSent(im.actor.model.api.Peer _peer, long rid, long date) {
        Peer peer = convert(_peer);

        // Change message state in conversation
        conversationActor(peer).send(new ConversationActor.MessageSent(rid, date));

        // Notify Sender Actor
        sendActor().send(new SenderActor.MessageSent(peer, rid));

        // Send information to OwnReadActor about out message
        ownReadActor().send(new OwnReadActor.NewOutMessage(peer, rid, date, false));
    }

    public void onChatClear(im.actor.model.api.Peer _peer) {
        Peer peer = convert(_peer);

        // TODO: Notify own read actor
        // TODO: Notify send actor

        // Clearing conversation
        conversationActor(peer).send(new ConversationActor.ClearConversation());
    }

    public void onChatDelete(im.actor.model.api.Peer _peer) {
        Peer peer = convert(_peer);

        // TODO: Notify own read actor
        // TODO: Notify send actor

        // Deleting conversation
        conversationActor(peer).send(new ConversationActor.DeleteConversation());
    }

    public void onUserRegistered(int uid, long date) {
        // TODO: New rid
        long rid = RandomUtils.nextRid();
        Message message = new Message(rid, date, date, uid,
                MessageState.UNKNOWN, new ServiceUserRegistered());

        ownReadActor().send(new OwnReadActor
                .NewMessage(new Peer(PeerType.PRIVATE, uid), rid, date, false));
        conversationActor(Peer.user(uid)).send(message);
    }
}