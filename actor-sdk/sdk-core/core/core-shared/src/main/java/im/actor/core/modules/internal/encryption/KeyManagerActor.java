package im.actor.core.modules.internal.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiEncryptionKey;
import im.actor.core.api.ApiEncryptionKeySignature;
import im.actor.core.api.rpc.RequestCreateNewKeyGroup;
import im.actor.core.api.rpc.ResponseCreateNewKeyGroup;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.encryption.entity.EncryptionKey;
import im.actor.core.modules.internal.encryption.entity.EphermalEncryptionKey;
import im.actor.core.modules.internal.encryption.entity.PrivateKeyStorage;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.storage.KeyValueRecord;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.sdk.util.Randoms;

public class KeyManagerActor extends ModuleActor {

    private static final String PRIVATE_KEYS = "private_keys";

    private static final String TAG = "KeyManagerActor";

    private PrivateKeyStorage privateKeyStorage;
    private KeyValueStorage ephermalStorage;

    public KeyManagerActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        ephermalStorage = Storage.createKeyValue("ephermal_keys");

        byte[] data = preferences().getBytes(PRIVATE_KEYS);
        if (data != null) {
            try {
                privateKeyStorage = new PrivateKeyStorage(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (privateKeyStorage == null) {
            Log.d(TAG, "Generating new encryption keys...");

            EncryptionKey identityKey = new EncryptionKey(Randoms.randomId(),
                    Curve25519.keyGen(Crypto.randomBytes(64)));
            ArrayList<EncryptionKey> keyPairs = new ArrayList<EncryptionKey>();
            keyPairs.add(new EncryptionKey(Randoms.randomId(),
                    Curve25519.keyGen(Crypto.randomBytes(64))));

            privateKeyStorage = new PrivateKeyStorage(identityKey, keyPairs, 0);
            preferences().putBytes(PRIVATE_KEYS, privateKeyStorage.toByteArray());
        }

        if (privateKeyStorage.getKeyGroupId() == 0) {
            Log.d(TAG, "Uploading main encryption keys...");

            EncryptionKey identityKey = privateKeyStorage.getIdentityKey();
            ApiEncryptionKey apiEncryptionKey =
                    new ApiEncryptionKey(
                            identityKey.getKeyId(),
                            identityKey.getKeyAlg(),
                            identityKey.getPublicKey(),
                            null);

            ArrayList<String> encryption = new ArrayList<String>();
            encryption.add("curve25519");
            encryption.add("Ed25519");
            encryption.add("kuznechik128");
            encryption.add("streebog256");
            encryption.add("sha256");
            encryption.add("sha512");
            encryption.add("aes128");

            ArrayList<ApiEncryptionKey> keys = new ArrayList<ApiEncryptionKey>();
            ArrayList<ApiEncryptionKeySignature> keySignatures = new ArrayList<ApiEncryptionKeySignature>();
            for (EncryptionKey key : privateKeyStorage.getKeys()) {
                ApiEncryptionKey apiKey =
                        new ApiEncryptionKey(
                                key.getKeyId(),
                                key.getKeyAlg(),
                                key.getPublicKey(),
                                null);
                keys.add(apiKey);


                byte[] signature = Curve25519.calculateSignature(Crypto.randomBytes(64), identityKey.getPrivateKey(), apiKey.toByteArray());
                keySignatures.add(
                        new ApiEncryptionKeySignature(
                                key.getKeyId(),
                                "Ed25519",
                                signature));
            }

            request(new RequestCreateNewKeyGroup(apiEncryptionKey, encryption, keys, keySignatures), new RpcCallback<ResponseCreateNewKeyGroup>() {
                @Override
                public void onResult(ResponseCreateNewKeyGroup response) {
                    privateKeyStorage = privateKeyStorage.markUploaded(response.getKeyGroupId());
                    preferences().putBytes(PRIVATE_KEYS, privateKeyStorage.toByteArray());
                    onMainKeysReady();
                }

                @Override
                public void onError(RpcException e) {
                    Log.w(TAG, "Keys upload error");
                    Log.e(TAG, e);

                    // Just ignore
                }
            });
        } else {
            onMainKeysReady();
        }
    }

    private void onMainKeysReady() {
        Log.d(TAG, "Main Keys are ready");

        // Generating ephemeral keys
        List<KeyValueRecord> records = ephermalStorage.loadAllItems();
        for (int i = 0; i < records.size() - 100; i++) {
            long randomId = Randoms.randomId();
            EncryptionKey encryptionKey = new EncryptionKey(
                    randomId,
                    Curve25519.keyGen(Crypto.randomBytes(64)));
            EphermalEncryptionKey ephermalEncryptionKey =
                    new EphermalEncryptionKey(encryptionKey, false);
            ephermalStorage.addOrUpdateItem(randomId, ephermalEncryptionKey.toByteArray());
        }

        // Uploading ephemeral keys
        records = ephermalStorage.loadAllItems();

        // TODO: Implement uploading
    }
}