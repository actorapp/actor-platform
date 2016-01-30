package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedMessage;
import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.api.ApiMessage;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.EncryptedBoxKey;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.promise.PromiseResolver;

public class EncryptedMsgActor extends ModuleActor {

    private static final String TAG = "MessageEncryptionActor";

    public EncryptedMsgActor(ModuleContext context) {
        super(context);
    }

    private void doEncrypt(int uid, ApiMessage message, final PromiseResolver future) {
        Log.d(TAG, "doEncrypt");
        try {
            ask(context().getEncryption().getEncryptedChatManager(uid), new EncryptedPeerActor.EncryptBox(message.buildContainer()), new AskCallback() {
                @Override
                public void onResult(Object obj) {
                    Log.d(TAG, "doEncrypt:onResult");
                    EncryptedPeerActor.EncryptBoxResponse encryptedBox = (EncryptedPeerActor.EncryptBoxResponse) obj;
                    ArrayList<ApiEncyptedBoxKey> boxKeys = new ArrayList<ApiEncyptedBoxKey>();
                    for (EncryptedBoxKey b : encryptedBox.getBox().getKeys()) {
                        boxKeys.add(new ApiEncyptedBoxKey(b.getUid(),
                                b.getKeyGroupId(), "curve25519", b.getEncryptedKey()));
                    }
                    ArrayList<Integer> ignored = new ArrayList<Integer>();
                    ApiEncryptedBox apiEncryptedBox = new ApiEncryptedBox(0, boxKeys, ignored, "aes-kuznechik", encryptedBox.getBox().getEncryptedPackage());
                    ApiEncryptedMessage apiEncryptedMessage = new ApiEncryptedMessage(apiEncryptedBox);
                    future.result(new EncryptedMessage(apiEncryptedMessage));
                }

                @Override
                public void onError(Exception e) {
                    Log.d(TAG, "doEncrypt:onError");
                    e.printStackTrace();
                    future.error(e);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        EncryptedBox encryptedBox = new EncryptedBox(encryptedBoxKeys.toArray(new EncryptedBoxKey[0]), message.getBox().getEncPackage());
        ask(context().getEncryption().getEncryptedChatManager(uid), new EncryptedPeerActor.DecryptBox(encryptedBox), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                Log.d(TAG, "onDecrypt:onResult in " + (Runtime.getActorTime() - start) + " ms");
                EncryptedPeerActor.DecryptBoxResponse re = (EncryptedPeerActor.DecryptBoxResponse) obj;
                try {
                    ApiMessage message = ApiMessage.fromBytes(re.getData());
                    Log.d(TAG, "onDecrypt:onResult " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "onDecrypt:onError");
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onAsk(Object message, PromiseResolver future) {
        if (message instanceof EncryptMessage) {
            doEncrypt(((EncryptMessage) message).getUid(), ((EncryptMessage) message).getMessage(),
                    future);
        } else {
            super.onAsk(message, future);
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
