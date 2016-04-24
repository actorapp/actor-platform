package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedBoxSignature;
import im.actor.core.api.ApiEncryptedMessage;
import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.api.ApiMessage;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.EncryptedBoxKey;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;

public class EncryptedMsgActor extends ModuleActor {

    private static final String TAG = "MessageEncryptionActor";

    public EncryptedMsgActor(ModuleContext context) {
        super(context);
    }

    private Promise doEncrypt(int uid, ApiMessage message) throws IOException {
        Log.d(TAG, "doEncrypt");

//        return ask(context().getEncryption().getEncryptedChatManager(uid), new EncryptedPeerActor.EncryptBox(message.buildContainer()))
//                .map(new Function<EncryptedPeerActor.EncryptBoxResponse, EncryptedMessage>() {
//                    @Override
//                    public EncryptedMessage apply(EncryptedPeerActor.EncryptBoxResponse encryptBoxResponse) {
//                        Log.d(TAG, "doEncrypt:onResult");
//                        ArrayList<ApiEncyptedBoxKey> boxKeys = new ArrayList<ApiEncyptedBoxKey>();
//                        for (EncryptedBoxKey b : encryptBoxResponse.getBox().getKeys()) {
//                            boxKeys.add(new ApiEncyptedBoxKey(b.getUid(),
//                                    b.getKeyGroupId(), "curve25519", b.getEncryptedKey()));
//                        }
//                        ApiEncryptedBox apiEncryptedBox = new ApiEncryptedBox(0, boxKeys, "aes-kuznechik", encryptBoxResponse.getBox().getEncryptedPackage(),
//                                new ArrayList<ApiEncryptedBoxSignature>());
//                        ApiEncryptedMessage apiEncryptedMessage = new ApiEncryptedMessage(apiEncryptedBox);
//                        return new EncryptedMessage(apiEncryptedMessage);
//                    }
//                });

        // TODO: Implement
        return null;
    }

    public void onDecrypt(int uid, ApiEncryptedMessage message) {
        Log.d(TAG, "onDecrypt:" + uid);
        final long start = im.actor.runtime.Runtime.getActorTime();
        ArrayList<EncryptedBoxKey> encryptedBoxKeys = new ArrayList<EncryptedBoxKey>();
        for (ApiEncyptedBoxKey key : message.getBox().getKeys()) {
            if (key.getUsersId() == myUid()) {
                encryptedBoxKeys.add(new EncryptedBoxKey(key.getUsersId(), key.getKeyGroupId(),
                        key.getAlgType(), key.getEncryptedKey()));
            }
        }
        final EncryptedBox encryptedBox = new EncryptedBox(encryptedBoxKeys.toArray(new EncryptedBoxKey[0]), message.getBox().getEncPackage());

        // TODO: Implement
//        ask(context().getEncryption().getEncryptedChatManager(uid), new EncryptedPeerActor.DecryptBox(encryptedBox), new AskCallback() {
//            @Override
//            public void onResult(Object obj) {
//                Log.d(TAG, "onDecrypt:onResult in " + (Runtime.getActorTime() - start) + " ms");
//                EncryptedPeerActor.DecryptBoxResponse re = (EncryptedPeerActor.DecryptBoxResponse) obj;
//                try {
//                    ApiMessage message = ApiMessage.fromBytes(re.getData());
//                    Log.d(TAG, "onDecrypt:onResult " + message);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.d(TAG, "onDecrypt:onError");
//                e.printStackTrace();
//            }
//        });
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof EncryptMessage) {
            return doEncrypt(((EncryptMessage) message).getUid(), ((EncryptMessage) message).getMessage());
        } else {
            return super.onAsk(message);
        }
    }

    @Override
    public void onReceive(Object message) {
        Log.d(TAG, "msg: " + message);
        if (message instanceof InMessage) {
            InMessage inMessage = (InMessage) message;
            onDecrypt(inMessage.senderUid, inMessage.encryptedMessage);
        } else {
            super.onReceive(message);
        }
    }

    public static class InMessage {

        private Peer peer;
        private long date;
        private int senderUid;
        private long rid;
        private ApiEncryptedMessage encryptedMessage;

        public InMessage(Peer peer, long date, int senderUid, long rid, ApiEncryptedMessage encryptedMessage) {
            this.peer = peer;
            this.date = date;
            this.senderUid = senderUid;
            this.rid = rid;
            this.encryptedMessage = encryptedMessage;
        }
    }

    public static class EncryptMessage {

        private int uid;
        private ApiMessage message;

        public EncryptMessage(int uid, ApiMessage message) {
            this.uid = uid;
            this.message = message;
        }

        public int getUid() {
            return uid;
        }

        public ApiMessage getMessage() {
            return message;
        }
    }

    public static class EncryptedMessage {
        private ApiEncryptedMessage encryptedMessage;

        public EncryptedMessage(ApiEncryptedMessage encryptedMessage) {
            this.encryptedMessage = encryptedMessage;
        }

        public ApiEncryptedMessage getEncryptedMessage() {
            return encryptedMessage;
        }
    }

    public static class DecryptMessage {

        private ApiEncryptedMessage encryptedMessage;

        public DecryptMessage(ApiEncryptedMessage encryptedMessage) {
            this.encryptedMessage = encryptedMessage;
        }

        public ApiEncryptedMessage getEncryptedMessage() {
            return encryptedMessage;
        }
    }
}
