package im.actor.core.modules.encryption;

import java.util.ArrayList;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.OwnPrivateKey;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.modules.encryption.entity.UserPublicKey;
import im.actor.core.modules.encryption.session.EncryptedSession;
import im.actor.core.modules.encryption.session.EncryptedSessionChain;
import im.actor.core.util.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.runtime.*;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Future;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.crypto.Curve25519;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.crypto.ratchet.RatchetMasterSecret;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;

public class EncryptedSessionActor extends ModuleActor {

    private final String TAG;

    //
    // Key References
    //

    private final long ownKey0;
    private final int uid;
    private final long theirKey0;
    private final int theirKeyGroup;

    //
    // Loaded Keys
    //

    private OwnPrivateKey ownIdentityKey;
    private OwnPrivateKey ownPreKey;
    private UserPublicKey theirIdentityKey;
    private UserPublicKey theirPreKey;
    private EncryptedSession session;
    // True if it is unable to load key
    private boolean isUnavailable = false;

    //
    // Temp encryption chains
    //

    private byte[] theirEphemeralKey;
    private ArrayList<EncryptedSessionChain> chains = new ArrayList<EncryptedSessionChain>();

    public EncryptedSessionActor(ModuleContext context, int uid, long ownKey0, long theirKey0,
                                 int theirKeyGroup) {
        super(context);
        this.TAG = "EncryptionSessionActor#" + uid + "_" + theirKeyGroup;
        this.uid = uid;
        this.ownKey0 = ownKey0;
        this.theirKey0 = theirKey0;
        this.theirKeyGroup = theirKeyGroup;
    }

    @Override
    public void preStart() {
        Log.d(TAG, "preStart");
        loadOwnIdentity();
    }

    private void loadOwnIdentity() {
        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchOwnKey(), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                ownIdentityKey = ((KeyManagerActor.FetchOwnKeyResult) obj).getIdentityKey();
                loadOwnPreKey();
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Own identity error");
                Log.e(TAG, e);
                isUnavailable = true;
            }
        });
    }

    private void loadOwnPreKey() {
        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchEphemeralPrivateKeyById(ownKey0), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                KeyManagerActor.FetchEphemeralPrivateKeyRes keyRes = (KeyManagerActor.FetchEphemeralPrivateKeyRes) obj;
                ownPreKey = new OwnPrivateKey(ownKey0, "curve25519", keyRes.getPrivateKey());
                loadTheirIdentity();
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Own pre key error");
                Log.e(TAG, e);
                isUnavailable = true;
            }
        });
    }

    private void loadTheirIdentity() {
        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchUserKeyGroups(uid), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                KeyManagerActor.FetchUserKeyGroupsResponse keyGroups = (KeyManagerActor.FetchUserKeyGroupsResponse) obj;
                UserKeysGroup keysGroup = null;
                for (UserKeysGroup g : keyGroups.getUserKeys().getUserKeysGroups()) {
                    if (g.getKeyGroupId() == theirKeyGroup) {
                        keysGroup = g;
                        break;
                    }
                }
                if (keysGroup == null) {
                    Log.w(TAG, "Their key group not found");
                    isUnavailable = true;
                    return;
                }

                theirIdentityKey = keysGroup.getIdentityKey();
                loadTheirKey0();
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Their key groups error");
                Log.e(TAG, e);
                isUnavailable = true;
            }
        });
    }

    private void loadTheirKey0() {
        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchUserEphemeralKey(uid, theirKeyGroup, theirKey0), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                KeyManagerActor.FetchUserEphemeralKeyResponse r = (KeyManagerActor.FetchUserEphemeralKeyResponse) obj;
                theirPreKey = r.getEphemeralKey();
                loadMasterKey();
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Their pre key error");
                Log.e(TAG, e);
                isUnavailable = true;
            }
        });
    }

    private void loadMasterKey() {
        session = new EncryptedSession(ownIdentityKey, ownPreKey,
                theirIdentityKey, theirPreKey, theirKeyGroup);
    }

    private void onEncrypt(final byte[] data, final Future future) {
        if (isUnavailable) {
            future.onError(new RuntimeException("Encryption session is unavailable"));
            return;
        }

//        if (theirEphemeralKey == null) {
//            ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchUserEphemeralKeyRandom(uid, theirKeyGroup), new AskCallback() {
//                @Override
//                public void onResult(Object obj) {
//                    if (theirEphemeralKey != null) {
//                        KeyManagerActor.FetchUserEphemeralKeyResponse response = (KeyManagerActor.FetchUserEphemeralKeyResponse) obj;
//                        theirEphemeralKey = response.getEphemeralKey().getPublicKey();
//                    }
//                    onEncrypt(data, future);
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    future.onError(e);
//                }
//            });
//            return;
//        }
//
//        if (chains.size() == 0) {
//            spawnChain(Curve25519.keyGenPrivate(Crypto.randomBytes(32)));
//        }
//
//        chains.get(0).getChain().send(new EncryptedSessionChainActor.EncryptMessage(data,
//                theirEphemeralKey, 0));

//        if (!assumeEnabled(new Runnable() {
//            @Override
//            public void run() {
//                onEncrypt(data, future);
//            }
//        })) {
//            return;
//        }
//
//        onEncrypt(data,
//                ownEphermalKey0,
//                currentOwnKey,
//                theirEphermalKey0,
//                currentTheirKey,
//                future);
    }

    private void onEncrypt(byte[] data,
                           OwnPrivateKey ownEphermalKey0,
                           OwnPrivateKey ownEphermalKey,
                           UserPublicKey theirEphermalKey0,
                           UserPublicKey theirEphermalKey,
                           Future future) {

//        Log.w(TAG, "Encrypting with: OwnKey0: " + ownEphermalKey0.getKeyId());
//        Log.w(TAG, "Encrypting with: TheirKey0: " + theirEphermalKey0.getKeyId());
//
//        byte[] master_secret = RatchetMasterSecret.calculateMasterSecret(
//                new RatchetPrivateKey(ownIdentityKey.getKey()),
//                new RatchetPrivateKey(ownEphermalKey0.getKey()),
//                new RatchetPublicKey(theirIdentityKey.getPublicKey()),
//                new RatchetPublicKey(theirEphermalKey0.getPublicKey()));
//        byte[] rootChainKey = RatchetRootChainKey.makeRootChainKey(
//                new RatchetPrivateKey(ownEphermalKey.getKey()),
//                new RatchetPublicKey(theirEphermalKey.getPublicKey()),
//                master_secret);
//
//        int messageIndex = outIndex++;
//
//        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rootChainKey, messageIndex);
//
//        Log.d(TAG, "MS: " + Hex.toHex(master_secret));
//        Log.d(TAG, "MS_11: " + Hex.toHex(Curve25519.keyGenPublic(ownIdentityKey.getKey())));
//        Log.d(TAG, "MS_21: " + Hex.toHex(Curve25519.keyGenPublic(ownEphermalKey0.getKey())));
//        Log.d(TAG, "MS_31: " + Hex.toHex(encryptionKeyGroup.getIdentityKey().getPublicKey()));
//        Log.d(TAG, "MS_41: " + Hex.toHex(theirEphermalKey.getPublicKey()));
//        Log.d(TAG, "RC: " + Hex.toHex(rootChainKey));
//        Log.d(TAG, "RC_1: " + Hex.toHex(Curve25519.keyGenPublic(ownEphermalKey.getKey())));
//        Log.d(TAG, "RC_2: " + Hex.toHex(theirEphermalKey.getPublicKey()));
//
//        Log.d(TAG, "AES: " + Hex.toHex(ratchetMessageKey.getKeyAES()));
//        Log.d(TAG, "AES_MAC: " + Hex.toHex(ratchetMessageKey.getMacAES()));
//        Log.d(TAG, "KUZ: " + Hex.toHex(ratchetMessageKey.getKeyKuz()));
//        Log.d(TAG, "KUZ_MAC: " + Hex.toHex(ratchetMessageKey.getMacKuz()));
//
//        byte[] header = ByteStrings.merge(
//                ByteStrings.intToBytes(encryptionKeyGroup.getKeyGroupId()),
//                ByteStrings.longToBytes(ownEphermalKey0.getKeyId()), /*Alice Initial Ephermal*/
//                ByteStrings.longToBytes(theirEphermalKey0.getKeyId()), /*Bob Initial Ephermal*/
//                Curve25519.keyGenPublic(ownEphermalKey.getKey()),
//                theirEphermalKey.getPublicKey(),
//                ByteStrings.intToBytes(messageIndex)); /* Message Index */
//
//        byte[] encrypted;
//        try {
//            encrypted = ActorBox.closeBox(header, data, Crypto.randomBytes(32), ratchetMessageKey);
//        } catch (IntegrityException e) {
//            e.printStackTrace();
//            future.onError(e);
//            return;
//        }
//
//        future.onResult(new EncryptedPackageRes(ByteStrings.merge(header, encrypted)));
    }

    private void onDecrypt(final byte[] data, final Future future) {
        if (isUnavailable) {
            future.onError(new RuntimeException("Encryption session is unavailable"));
            return;
        }

        // final int ownKeyGroupId = ByteStrings.bytesToInt(data, 0);
        // final long ownEphermalKey0Id = ByteStrings.bytesToLong(data, 4);
        // final long theirEphermalKey0Id = ByteStrings.bytesToLong(data, 12);
        final byte[] senderEphemeralKey = ByteStrings.substring(data, 20, 32);
        final byte[] receiverEphemeralKey = ByteStrings.substring(data, 52, 32);
        final int messageIndex = ByteStrings.bytesToInt(data, 84);

        EncryptedSessionChain pickedChain = null;
        for (EncryptedSessionChain c : chains) {
            if (ByteStrings.isEquals(Curve25519.keyGenPublic(c.getOwnPrivateKey()), receiverEphemeralKey)) {
                pickedChain = c;
                break;
            }
        }

        if (pickedChain == null) {
            ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchEphemeralPrivateKey(receiverEphemeralKey), new AskCallback() {

                @Override
                public void onResult(Object obj) {
                    final KeyManagerActor.FetchEphemeralPrivateKeyRes theirEphermalKey = (KeyManagerActor.FetchEphemeralPrivateKeyRes) obj;
                    spawnChain(theirEphermalKey.getPrivateKey(), senderEphemeralKey);
                    onDecrypt(data, future);
                }

                @Override
                public void onError(Exception e) {
                    future.onError(e);
                }
            });
            return;
        }

        try {
            future.onResult(new DecryptedPackage(pickedChain.decrypt(data)));
        } catch (IntegrityException e) {
            e.printStackTrace();
            future.onError(e);
        }

//        ask(pickedChain.getChain(), new EncryptedSessionChainActor.DecryptMessage(
//                ByteStrings.substring(data, 0, 88),
//                ByteStrings.substring(data, 88, data.length - 88),
//                senderEphemeralKey, messageIndex), new AskCallback() {
//            @Override
//            public void onResult(Object obj) {
//                // Updating ephemeral key
//                theirEphemeralKey = senderEphemeralKey;
//                future.onResult();
//            }
//
//            @Override
//            public void onError(Exception e) {
//                future.onError(e);
//            }
//        });
    }

    private EncryptedSessionChain spawnChain(final byte[] privateKey, final byte[] publicKey) {
        EncryptedSessionChain chain = new EncryptedSessionChain(session, privateKey, publicKey);
        chains.add(0, chain);
        return chain;
    }

    @Override
    public boolean onAsk(Object message, Future future) {
        Log.d(TAG, "onAsk");
        if (message instanceof EncryptPackage) {
            onEncrypt(((EncryptPackage) message).getData(), future);
            return false;
        } else if (message instanceof DecryptPackage) {
            DecryptPackage decryptPackage = (DecryptPackage) message;
            onDecrypt(decryptPackage.getData(), future);
            return false;
        } else {
            return super.onAsk(message, future);
        }
    }

    public static class EncryptPackage {
        private byte[] data;

        public EncryptPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class EncryptedPackageRes {

        private byte[] data;

        public EncryptedPackageRes(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class DecryptPackage {

        private byte[] data;

        public DecryptPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

    public static class DecryptedPackage {

        private byte[] data;

        public DecryptedPackage(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }
    }

}