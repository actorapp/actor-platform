package im.actor.core.modules.encryption.ratchet;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.api.ApiMessage;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.EncryptedBoxKey;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.promise.Promise;

public class EncryptedMsgActor extends ModuleActor {

    private static final String TAG = "MessageEncryptionActor";

    public EncryptedMsgActor(ModuleContext context) {
        super(context);
    }

    private Promise<ApiEncryptedBox> doEncrypt(int uid, ApiMessage message) throws IOException {
        return context().getEncryption().getEncryptedUser(uid).encrypt(message.buildContainer())
                .map(encryptBoxResponse -> {
                    ArrayList<ApiEncyptedBoxKey> boxKeys = new ArrayList<>();
                    for (EncryptedBoxKey b : encryptBoxResponse.getKeys()) {
                        boxKeys.add(new ApiEncyptedBoxKey(b.getUid(),
                                b.getKeyGroupId(), "curve25519", b.getEncryptedKey()));
                    }
                    return new ApiEncryptedBox(0,
                            boxKeys, "aes-kuznechik",
                            encryptBoxResponse.getEncryptedPackage(),
                            new ArrayList<>());
                });
    }

    public Promise<ApiMessage> doDecrypt(int uid, ApiEncryptedBox box) {
        ArrayList<EncryptedBoxKey> encryptedBoxKeys = new ArrayList<>();
        for (ApiEncyptedBoxKey key : box.getKeys()) {
            if (key.getUsersId() == myUid()) {
                encryptedBoxKeys.add(new EncryptedBoxKey(key.getUsersId(), key.getKeyGroupId(),
                        key.getAlgType(), key.getEncryptedKey()));
            }
        }
        EncryptedBox encryptedBox = new EncryptedBox(
                encryptedBoxKeys.toArray(new EncryptedBoxKey[encryptedBoxKeys.size()]),
                box.getEncPackage());

        return context().getEncryption().getEncryptedUser(uid).decrypt(encryptedBox).map(bytes -> {
            try {
                return ApiMessage.fromBytes(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof EncryptMessage) {
            return doEncrypt(((EncryptMessage) message).getUid(), ((EncryptMessage) message).getMessage());
        } else if (message instanceof DecryptMessage) {
            return doDecrypt(((DecryptMessage) message).getUid(), ((DecryptMessage) message).getEncryptedBox());
        } else {
            return super.onAsk(message);
        }
    }

    public static class EncryptMessage implements AskMessage<ApiEncryptedBox> {

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

    public static class DecryptMessage implements AskMessage<ApiMessage> {

        private int uid;
        private ApiEncryptedBox encryptedBox;

        public DecryptMessage(int uid, ApiEncryptedBox encryptedBox) {
            this.uid = uid;
            this.encryptedBox = encryptedBox;
        }

        public int getUid() {
            return uid;
        }

        public ApiEncryptedBox getEncryptedBox() {
            return encryptedBox;
        }
    }
}
