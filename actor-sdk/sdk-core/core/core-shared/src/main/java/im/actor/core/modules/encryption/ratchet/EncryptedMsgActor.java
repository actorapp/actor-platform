package im.actor.core.modules.encryption.ratchet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedContent;
import im.actor.core.api.ApiEncryptedData;
import im.actor.core.api.ApiEncyptedBoxKey;
import im.actor.core.api.ApiKeyGroupId;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.encryption.ratchet.entity.EncryptedMessage;
import im.actor.core.modules.encryption.ratchet.entity.EncryptedUserKeys;
import im.actor.core.modules.encryption.ratchet.entity.OwnIdentity;
import im.actor.runtime.Crypto;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.crypto.Cryptos;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.prf.PRF;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromisesArray;

public class EncryptedMsgActor extends ModuleActor {

    private static final int VERSION = 1;

    private static final String TAG = "MessageEncryptionActor";

    private final PRF KEY_PRF = Cryptos.PRF_SHA_STREEBOG_256();

    private boolean isFreezed = false;
    private OwnIdentity ownIdentity;

    public EncryptedMsgActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();

        context().getEncryption().getKeyManager().getOwnIdentity().then(id -> {
            ownIdentity = id;
            isFreezed = false;
            unstashAll();
        });
    }

    private Promise<EncryptedMessage> doEncrypt(ApiEncryptedContent message, List<Integer> uids) throws IOException {

        // Generate Encryption Keys
        byte[] encKey = Crypto.randomBytes(32);
        byte[] encKeyExtended = KEY_PRF.calculate(encKey, "ActorPackage", 128);

        // Encrypt Data
        byte[] encryptedData;
        byte[] dataToEncrypt = message.toByteArray();
        byte[] dataHeader = ByteStrings.merge(new byte[]{VERSION},
                ByteStrings.intToBytes(myUid()),
                ByteStrings.intToBytes(ownIdentity.getKeyGroup()));
        try {
            encryptedData = ActorBox.closeBox(dataHeader, dataToEncrypt, Crypto.randomBytes(32),
                    new ActorBoxKey(encKeyExtended));
        } catch (IntegrityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Put To Plain-Text versioned container
        byte[] dataContainer = new ApiEncryptedData(VERSION, encryptedData).toByteArray();

        // Encryption for all required users
        return PromisesArray.of(uids)
                .map((u) -> Promise.success(context().getEncryption().getEncryptedUser(u)))
                .map((u) -> u.encrypt(encKeyExtended))
                .zip((r) -> {
                    ArrayList<ApiEncyptedBoxKey> boxKeys = new ArrayList<>();
                    ArrayList<ApiKeyGroupId> ignored = new ArrayList<>();
                    for (EncryptedUserKeys uk : r) {
                        boxKeys.addAll(uk.getBoxKeys());
                        for (Integer i : uk.getIgnoredKeys()) {
                            ignored.add(new ApiKeyGroupId(uk.getUid(), i));
                        }
                    }

                    return new EncryptedMessage(new ApiEncryptedBox(ownIdentity.getKeyGroup(),
                            boxKeys, "aes-kuznechik", dataContainer, new ArrayList<>()), ignored);
                });
    }

    public Promise<ApiEncryptedContent> doDecrypt(int uid, ApiEncryptedBox box) {

        // Loading Package
        ApiEncryptedData encData;
        try {
            encData = Bser.parse(new ApiEncryptedData(), box.getEncPackage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (encData.getVersion() != VERSION) {
            throw new RuntimeException("Unsupported version " + encData.getVersion());
        }

        return context().getEncryption()
                .getEncryptedUser(uid)
                .decrypt(box.getSenderKeyGroupId(), box.getKeys()).map(bytes -> {

                    // Decryption of package
                    byte[] dataHeader = ByteStrings.merge(
                            new byte[]{VERSION},
                            ByteStrings.intToBytes(myUid()),
                            ByteStrings.intToBytes(box.getSenderKeyGroupId()));
                    byte[] data;
                    try {
                        data = ActorBox.openBox(dataHeader, encData.getData(), new ActorBoxKey(bytes));
                    } catch (IntegrityException e) {
                        throw new RuntimeException(e);
                    }

                    // Parsing content
                    ApiEncryptedContent content;
                    try {
                        content = ApiEncryptedContent.fromBytes(data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return content;
                });
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof EncryptMessage) {
            if (isFreezed) {
                stash();
                return null;
            }
            return doEncrypt(((EncryptMessage) message).getMessage(), ((EncryptMessage) message).getUids());
        } else if (message instanceof DecryptMessage) {
            if (isFreezed) {
                stash();
                return null;
            }
            return doDecrypt(((DecryptMessage) message).getUid(), ((DecryptMessage) message).getEncryptedBox());
        } else {
            return super.onAsk(message);
        }
    }

    public static class EncryptMessage implements AskMessage<EncryptedMessage> {

        private ApiEncryptedContent message;
        private List<Integer> uids;

        public EncryptMessage(ApiEncryptedContent message, List<Integer> uids) {
            this.uids = uids;
            this.message = message;
        }

        public ApiEncryptedContent getMessage() {
            return message;
        }

        public List<Integer> getUids() {
            return uids;
        }
    }

    public static class DecryptMessage implements AskMessage<ApiEncryptedContent> {

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
