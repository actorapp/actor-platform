package im.actor.core.modules.encryption;

import java.util.ArrayList;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.OwnPrivateKey;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.modules.encryption.entity.UserPublicKey;
import im.actor.core.util.Hex;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.*;
import im.actor.runtime.actors.Future;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.crypto.ratchet.RatchetMasterSecret;
import im.actor.runtime.crypto.ratchet.RatchetMessageKey;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;
import im.actor.runtime.crypto.ratchet.RatchetRootChainKey;

public class EncryptedSessionActor extends ModuleActor {

    private final String TAG;

    private final int uid;

    private final long ownKey0;
    private final long theirKey0;
    private final int theirKeyGroup;

    private OwnPrivateKey ownIdentityKey;
    private OwnPrivateKey ownPreKey;
    private UserPublicKey theirIdentityKey;
    private UserPublicKey theirPreKey;

    private byte[] masterKey;

    private byte[] prevOwnKey;
    private OwnPrivateKey currentOwnKey;
    private UserPublicKey currentTheirKey;

    private int outIndex = 0;
    private int inIndex = 0;

    private boolean isUnavailable = false;

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
        masterKey = RatchetMasterSecret.calculateMasterSecret(
                new RatchetPrivateKey(ownIdentityKey.getKey()),
                new RatchetPrivateKey(ownPreKey.getKey()),
                new RatchetPublicKey(theirIdentityKey.getPublicKey()),
                new RatchetPublicKey(theirPreKey.getPublicKey()));
    }

    private void onEncrypt(final byte[] data, final Future future) {

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

        final long start = im.actor.runtime.Runtime.getActorTime();

        Log.d(TAG, "onDecrypt");
        Log.d(TAG, "onDecrypt: " + Hex.toHex(data));

        final int ownKeyGroupId = ByteStrings.bytesToInt(data, 0);
        final long ownEphermalKey0Id = ByteStrings.bytesToLong(data, 4);
        final long theirEphermalKey0Id = ByteStrings.bytesToLong(data, 12);
        final byte[] ownEphermalKey = ByteStrings.substring(data, 20, 32);
        final byte[] theirEphermalKey = ByteStrings.substring(data, 52, 32);
        final int messageIndex = ByteStrings.bytesToInt(data, 84);

        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchEphemeralPrivateKey(theirEphermalKey), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                final KeyManagerActor.FetchEphemeralPrivateKeyRes theirEphermalKey = (KeyManagerActor.FetchEphemeralPrivateKeyRes) obj;
                Log.d(TAG, "Before plain text in " + (im.actor.runtime.Runtime.getActorTime() - start) + " ms");

                byte[] rc = RatchetRootChainKey.makeRootChainKey(
                        new RatchetPrivateKey(theirEphermalKey.getPrivateKey()),
                        new RatchetPublicKey(ownEphermalKey),
                        masterKey);

                Log.d(TAG, "Before plain text 2 in " + (im.actor.runtime.Runtime.getActorTime() - start) + " ms");

                ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rc, messageIndex);

                Log.d(TAG, "Before plain text 3 in " + (im.actor.runtime.Runtime.getActorTime() - start) + " ms");

                byte[] header = ByteStrings.substring(data, 0, 88);
                byte[] pkg = ByteStrings.substring(data, 88, data.length - 88);

                byte[] plainText;
                try {
                    plainText = ActorBox.openBox(header, pkg, ratchetMessageKey);
                    Log.d(TAG, "Plain Text in " + (im.actor.runtime.Runtime.getActorTime() - start) + " ms");
                } catch (IntegrityException e) {
                    Log.d(TAG, "Plain Text error");
                    e.printStackTrace();
                    future.onError(e);
                    return;
                }

                future.onResult(new DecryptedPackage(plainText));
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "onDecrypt:onResultEphermal:onError");
                future.onError(e);
            }
        });


//        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rc, index);
//
//        Log.d(TAG, "MS: " + Hex.toHex(ms));
//        Log.d(TAG, "MS_11: " + Hex.toHex(Curve25519.keyGenPublic(ownIdentityKey.getKey())));
//        Log.d(TAG, "MS_21: " + Hex.toHex(Curve25519.keyGenPublic(ownEphemeralPrivateKey0)));
//        Log.d(TAG, "MS_31: " + Hex.toHex(encryptionKeyGroup.getIdentityKey().getPublicKey()));
//        Log.d(TAG, "MS_41: " + Hex.toHex(theirEphemeralKey0));
//
//        Log.d(TAG, "RC: " + Hex.toHex(rc));
//        Log.d(TAG, "RC_1: " + Hex.toHex(Curve25519.keyGenPublic(ownEphemeralPrivateKey)));
//        Log.d(TAG, "RC_2: " + Hex.toHex(theirEphemeralKey));
//
//        Log.d(TAG, "AES: " + Hex.toHex(ratchetMessageKey.getKeyAES()));
//        Log.d(TAG, "AES_MAC: " + Hex.toHex(ratchetMessageKey.getMacAES()));
//        Log.d(TAG, "KUZ: " + Hex.toHex(ratchetMessageKey.getKeyKuz()));
//        Log.d(TAG, "KUZ_MAC: " + Hex.toHex(ratchetMessageKey.getMacKuz()));
//
//        byte[] header = ByteStrings.substring(data, 0, 88);
//        byte[] pkg = ByteStrings.substring(data, 88, data.length - 88);
//
//        byte[] plainText;
//        try {
//            plainText = ActorBox.openBox(header, pkg, ratchetMessageKey);
//            Log.d(TAG, "Plain Text");
//        } catch (IntegrityException e) {
//            Log.d(TAG, "Plain Text error");
//            e.printStackTrace();
//            future.onError(e);
//            return;
//        }
//
//        future.onResult(new DecryptedPackage(plainText));


//
////        Log.d(TAG, "onDecrypt: Own " + Hex.toHex(ownEphermalKey));
////        Log.d(TAG, "onDecrypt: Their " + Hex.toHex(theirEphermalKey));
//
//        Log.d(TAG, "ownEphermalKey0Id: " + ownEphermalKey0Id);
//        Log.d(TAG, "theirEphermalKey0Id: " + theirEphermalKey0Id);
//
//        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchEphemeralPrivateKey(theirEphermalKey), new AskCallback() {
//            @Override
//            public void onResult(Object obj) {
//                final KeyManagerActor.FetchEphemeralPrivateKeyRes theirEphermalKey = (KeyManagerActor.FetchEphemeralPrivateKeyRes) obj;
//
//                ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchEphemeralPrivateKeyById(theirEphermalKey0Id),
//                        new AskCallback() {
//                            @Override
//                            public void onResult(Object obj) {
//
//                                final KeyManagerActor.FetchEphemeralPrivateKeyRes theirEphermalKey0
//                                        = (KeyManagerActor.FetchEphemeralPrivateKeyRes) obj;
//
//                                Log.d(TAG, "theirEphermalKey0: " + Hex.toHex(Curve25519.keyGenPublic(theirEphermalKey0.getPrivateKey())));
//
//                                ArrayList<Long> keys = new ArrayList<Long>();
//                                keys.add(ownEphermalKey0Id);
//                                request(new RequestLoadPublicKey(new ApiUserOutPeer(uid, getUser(uid).getAccessHash()), encryptionKeyGroup.getKeyGroupId(), keys), new RpcCallback<ResponsePublicKeys>() {
//                                    @Override
//                                    public void onResult(ResponsePublicKeys response) {
//                                        byte[] ownEphermalKey0 = response.getPublicKey().get(0).getKeyMaterial();
//                                        Log.d(TAG, "ownEphermalKey0: " + Hex.toHex(ownEphermalKey0));
//                                        onDecrypt(data,
//                                                ownEphermalKey0,
//                                                ownEphermalKey,
//                                                theirEphermalKey0.getPrivateKey(),
//                                                theirEphermalKey.getPrivateKey(),
//                                                messageIndex,
//                                                future);
//                                    }
//
//                                    @Override
//                                    public void onError(RpcException e) {
//                                        Log.d(TAG, "onDecrypt:RequestLoadPublicKey:onError");
//                                        future.onError(e);
//                                    }
//                                });
//
//                            }
//
//                            @Override
//                            public void onError(Exception e) {
//                                Log.d(TAG, "onDecrypt:onResultPrivate:onError");
//                                future.onError(e);
//                            }
//                        });
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.d(TAG, "onDecrypt:onResultEphermal:onError");
//                future.onError(e);
//            }
//        });

        // future.onResult();
    }

    private void onDecrypt(byte[] data,
                           byte[] theirEphemeralKey0,
                           byte[] theirEphemeralKey,
                           byte[] ownEphemeralPrivateKey0,
                           byte[] ownEphemeralPrivateKey,
                           int index,
                           Future future) {

        Log.d(TAG, "onDecrypt2");

//        byte[] ms = RatchetMasterSecret.calculateMasterSecret(
//                new RatchetPrivateKey(ownIdentityKey.getKey()),
//                new RatchetPrivateKey(ownEphemeralPrivateKey0),
//                new RatchetPublicKey(encryptionKeyGroup.getIdentityKey().getPublicKey()),
//                new RatchetPublicKey(theirEphemeralKey0));
//
//        byte[] rc = RatchetRootChainKey.makeRootChainKey(
//                new RatchetPrivateKey(ownEphemeralPrivateKey),
//                new RatchetPublicKey(theirEphemeralKey),
//                ms);
//
//        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rc, index);
//
//        Log.d(TAG, "MS: " + Hex.toHex(ms));
//        Log.d(TAG, "MS_11: " + Hex.toHex(Curve25519.keyGenPublic(ownIdentityKey.getKey())));
//        Log.d(TAG, "MS_21: " + Hex.toHex(Curve25519.keyGenPublic(ownEphemeralPrivateKey0)));
//        Log.d(TAG, "MS_31: " + Hex.toHex(encryptionKeyGroup.getIdentityKey().getPublicKey()));
//        Log.d(TAG, "MS_41: " + Hex.toHex(theirEphemeralKey0));
//
//        Log.d(TAG, "RC: " + Hex.toHex(rc));
//        Log.d(TAG, "RC_1: " + Hex.toHex(Curve25519.keyGenPublic(ownEphemeralPrivateKey)));
//        Log.d(TAG, "RC_2: " + Hex.toHex(theirEphemeralKey));
//
//        Log.d(TAG, "AES: " + Hex.toHex(ratchetMessageKey.getKeyAES()));
//        Log.d(TAG, "AES_MAC: " + Hex.toHex(ratchetMessageKey.getMacAES()));
//        Log.d(TAG, "KUZ: " + Hex.toHex(ratchetMessageKey.getKeyKuz()));
//        Log.d(TAG, "KUZ_MAC: " + Hex.toHex(ratchetMessageKey.getMacKuz()));
//
//        byte[] header = ByteStrings.substring(data, 0, 88);
//        byte[] pkg = ByteStrings.substring(data, 88, data.length - 88);
//
//        byte[] plainText;
//        try {
//            plainText = ActorBox.openBox(header, pkg, ratchetMessageKey);
//            Log.d(TAG, "Plain Text");
//        } catch (IntegrityException e) {
//            Log.d(TAG, "Plain Text error");
//            e.printStackTrace();
//            future.onError(e);
//            return;
//        }
//
//        future.onResult(new DecryptedPackage(plainText));
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