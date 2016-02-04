package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedBoxSignature;
import im.actor.core.api.ApiEncryptedData;
import im.actor.core.api.ApiEncryptedMessage;
import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.api.ApiKeyGroupId;
import im.actor.core.api.ApiMessage;
import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.EncryptedBoxKey;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.collections.ManagedList;
import im.actor.runtime.function.Constructor;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseResolver;

public class EncryptedActor extends ModuleActor {

    public static Constructor<EncryptedActor> CONSTRUCTOR(final ModuleContext context) {
        return new Constructor<EncryptedActor>() {
            @Override
            public EncryptedActor create() {
                return new EncryptedActor(context);
            }
        };
    }

    private static final String TAG = "EncryptedActor";

    public EncryptedActor(ModuleContext context) {
        super(context);
    }

    private Promise<CipherTextPackage> doEncrypt(final int uid, ApiEncryptedData message) throws IOException {
        Log.d(TAG, "doEncrypt");

        return ask(context().getEncryption().getEncryptedChatManager(uid), new EncryptedPeerActor.EncryptBox(message))
                .map(new Function<EncryptedPeerActor.EncryptBoxResponse, CipherTextPackage>() {
                    @Override
                    public CipherTextPackage apply(EncryptedPeerActor.EncryptBoxResponse encryptBoxResponse) {
                        Log.d(TAG, "doEncrypt:onResult");
                        ArrayList<ApiEncyptedBoxKey> boxKeys = new ArrayList<ApiEncyptedBoxKey>();
                        ArrayList<ApiKeyGroupId> ignored = new ArrayList<>();
                        for (EncryptedBoxKey b : encryptBoxResponse.getBox().getKeys()) {
                            boxKeys.add(new ApiEncyptedBoxKey(b.getUid(),
                                    b.getKeyGroupId(), "curve25519", b.getEncryptedKey()));
                        }
                        ;
                        for (int kgid : encryptBoxResponse.getIgnored()) {
                            ignored.add(new ApiKeyGroupId(uid, kgid));
                        }
                        ApiEncryptedBox apiEncryptedBox = new ApiEncryptedBox(encryptBoxResponse.getBox().getSenderKeyGroupId(), boxKeys, "aes-kuznechik", encryptBoxResponse.getBox().getEncryptedPackage(),
                                new ArrayList<ApiEncryptedBoxSignature>());
                        return new CipherTextPackage(apiEncryptedBox, ignored);
                    }
                });
    }

    public Promise<PlainTextPackage> doDecrypt(int uid, ApiEncryptedBox encryptedBox) {
        Log.d(TAG, "doDecrypt");

        ArrayList<EncryptedBoxKey> encryptedBoxKeys = ManagedList.of(encryptedBox.getKeys()).filter(new Predicate<ApiEncyptedBoxKey>() {
            @Override
            public boolean apply(ApiEncyptedBoxKey apiEncyptedBoxKey) {
                return apiEncyptedBoxKey.getUsersId() == myUid();
            }
        }).map(new Function<ApiEncyptedBoxKey, EncryptedBoxKey>() {
            @Override
            public EncryptedBoxKey apply(ApiEncyptedBoxKey key) {
                return new EncryptedBoxKey(key.getUsersId(), key.getKeyGroupId(),
                        key.getAlgType(), key.getEncryptedKey());
            }
        });

        if (encryptedBoxKeys.size() == 0) {
            throw new RuntimeException("No keys found");
        }

        EncryptedBox encryptedBox1 = new EncryptedBox(encryptedBox.getSenderKeyGroupId(), encryptedBoxKeys.toArray(new EncryptedBoxKey[encryptedBoxKeys.size()]), encryptedBox.getEncPackage());

        return ask(context().getEncryption().getEncryptedChatManager(uid), new EncryptedPeerActor.DecryptBox(encryptedBox1))
                .map(new Function<EncryptedPeerActor.DecryptBoxResponse, PlainTextPackage>() {
                    @Override
                    public PlainTextPackage apply(EncryptedPeerActor.DecryptBoxResponse decryptBoxResponse) {
                        return new PlainTextPackage(decryptBoxResponse.getData());
                    }
                });
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof DoEncryptPackage) {
            return doEncrypt(((DoEncryptPackage) message).getReceiverUid(), ((DoEncryptPackage) message).getData());
        } else if (message instanceof DoDecryptPackage) {
            return doDecrypt(((DoDecryptPackage) message).getSenderUid(), ((DoDecryptPackage) message).getEncryptedBox());
        } else {
            return super.onAsk(message);
        }
    }

    public static class DoDecryptPackage implements AskMessage<PlainTextPackage> {

        private int senderUid;
        private ApiEncryptedBox encryptedBox;

        public DoDecryptPackage(int senderUid, ApiEncryptedBox encryptedBox) {
            this.senderUid = senderUid;
            this.encryptedBox = encryptedBox;
        }

        public int getSenderUid() {
            return senderUid;
        }

        public ApiEncryptedBox getEncryptedBox() {
            return encryptedBox;
        }
    }

    public static class PlainTextPackage {

        private ApiEncryptedData data;

        public PlainTextPackage(ApiEncryptedData data) {
            this.data = data;
        }

        public ApiEncryptedData getData() {
            return data;
        }
    }

    public static class DoEncryptPackage implements AskMessage<CipherTextPackage> {

        private int receiverUid;
        private ApiEncryptedData data;

        public DoEncryptPackage(int receiverUid, ApiEncryptedData data) {
            this.receiverUid = receiverUid;
            this.data = data;
        }

        public int getReceiverUid() {
            return receiverUid;
        }

        public ApiEncryptedData getData() {
            return data;
        }
    }

    public static class CipherTextPackage {

        private ApiEncryptedBox apiEncryptedBox;
        private ArrayList<ApiKeyGroupId> ignoredKeyGroups;

        public CipherTextPackage(ApiEncryptedBox apiEncryptedBox, ArrayList<ApiKeyGroupId> ignoredKeyGroups) {
            this.apiEncryptedBox = apiEncryptedBox;
            this.ignoredKeyGroups = ignoredKeyGroups;
        }

        public ArrayList<ApiKeyGroupId> getIgnoredKeyGroups() {
            return ignoredKeyGroups;
        }

        public ApiEncryptedBox getApiEncryptedBox() {
            return apiEncryptedBox;
        }
    }
}