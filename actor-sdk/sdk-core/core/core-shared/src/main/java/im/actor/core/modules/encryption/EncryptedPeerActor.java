package im.actor.core.modules.encryption;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestLoadPublicKeyGroups;
import im.actor.core.api.rpc.ResponsePublicKeyGroups;
import im.actor.core.entity.User;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.EncryptedBox;
import im.actor.core.modules.encryption.entity.EncryptedBoxKey;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.core.modules.encryption.entity.UserKeysGroup;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.Hex;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Future;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.actors.ask.AskRequest;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.primitives.util.ByteStrings;

public class EncryptedPeerActor extends ModuleActor {

    private final String TAG;

    private int uid;

    private UserKeys userKeys;
    private HashMap<Integer, ActorRef> sessions = new HashMap<Integer, ActorRef>();
    private int ownKeyGroupId;

    public EncryptedPeerActor(int uid, ModuleContext context) {
        super(context);
        TAG = "EncryptedPeerActor#" + uid;
        this.uid = uid;
    }

    @Override
    public void preStart() {
        super.preStart();

        Log.d(TAG, "Loading groups...");
        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchUserKeyGroups(uid), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                KeyManagerActor.FetchUserKeyGroupsResponse response = (KeyManagerActor.FetchUserKeyGroupsResponse) obj;
                userKeys = response.getUserKeys();
                onGroupsReady();
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "Unable to load key groups");
                Log.e(TAG, e);
            }
        });
    }

    private void onGroupsReady() {
        Log.w(TAG, "Groups ready #" + userKeys.getUserKeysGroups().length);

        ask(context().getEncryption().getKeyManager(), new KeyManagerActor.FetchOwnKeyGroup(), new AskCallback() {
            @Override
            public void onResult(Object obj) {
                Log.w(TAG, "FetchOwnKeyGroup: onResult");
                KeyManagerActor.FetchOwnKeyGroupResult res = (KeyManagerActor.FetchOwnKeyGroupResult) obj;
                ownKeyGroupId = res.getKeyGroupId();
                onOwnKeysReady();
            }

            @Override
            public void onError(Exception e) {
                // Do nothing
                Log.w(TAG, "FetchOwnKeyGroup: onError");
                Log.e(TAG, e);
            }
        });
    }

    private void onOwnKeysReady() {
        Log.w(TAG, "onOwnKeysReady");

        for (final UserKeysGroup g : userKeys.getUserKeysGroups()) {
            sessions.put(g.getKeyGroupId(), system().actorOf(Props.create(EncryptedSessionActor.class, new ActorCreator<EncryptedSessionActor>() {
                @Override
                public EncryptedSessionActor create() {
                    return new EncryptedSessionActor(context(), uid, g);
                }
            }), getPath() + "/k_" + g.getKeyGroupId()));
        }
    }

    private void doEncrypt(final byte[] data, final Future future) {
        Log.d(TAG, "doEncrypt");
        final byte[] encKey = Crypto.randomBytes(128);

        final ArrayList<EncryptedBoxKey> encryptedKeys = new ArrayList<EncryptedBoxKey>();
        for (final Integer keyGroup : sessions.keySet()) {
            ask(sessions.get(keyGroup), new EncryptedSessionActor.EncryptPackage(encKey), new AskCallback() {
                @Override
                public void onResult(Object obj) {
                    EncryptedSessionActor.EncryptedPackageRes res = (EncryptedSessionActor.EncryptedPackageRes) obj;
                    encryptedKeys.add(new EncryptedBoxKey(uid, keyGroup, res.getData()));
                    if (encryptedKeys.size() == sessions.size()) {
                        doEncrypt(encKey, data, encryptedKeys, future);
                    }
                }

                @Override
                public void onError(Exception e) {
                    future.onError(e);
                }
            });
        }
    }

    private void doEncrypt(byte[] encKey, byte[] data, ArrayList<EncryptedBoxKey> encryptedKeys, Future future) {
        Log.d(TAG, "doEncrypt2");
        byte[] encData;
        try {
            encData = ActorBox.closeBox(ByteStrings.intToBytes(ownKeyGroupId), data, Crypto.randomBytes(32), new ActorBoxKey(encKey));
        } catch (IntegrityException e) {
            e.printStackTrace();
            future.onError(e);
            return;
        }

        EncryptedBox encryptedBox = new EncryptedBox(
                encryptedKeys.toArray(new EncryptedBoxKey[encryptedKeys.size()]),
                ByteStrings.merge(ByteStrings.intToBytes(ownKeyGroupId), encData));

        Log.d(TAG, "doEncrypt:EncPackage: " + Hex.toHex(encData));
        for (EncryptedBoxKey k : encryptedKeys) {
            Log.d(TAG, "Key: " + Hex.toHex(k.getEncryptedKey()));
        }

        future.onResult(encryptedBox);
    }

    private void doDecrypt(EncryptedBox data, final Future future) {
        int senderKeyGroup = ByteStrings.bytesToInt(ByteStrings.substring(data.getEncryptedPackage(), 0, 4));
        byte[] encPackage = ByteStrings.substring(data.getEncryptedPackage(), 4, data.getEncryptedPackage().length - 4);

        if (sessions.containsKey(senderKeyGroup)) {
            Log.d(TAG, "Decryption with key group");
            byte[] encKey = null;
            for (EncryptedBoxKey k : data.getKeys()) {
                if (k.getKeyGroupId() == ownKeyGroupId && k.getUid() == myUid()) {
                    encKey = k.getEncryptedKey();
                    break;
                }
            }

            Log.d(TAG, "EncPackage: " + Hex.toHex(encPackage));
            for (EncryptedBoxKey k : data.getKeys()) {
                Log.d(TAG, "Key: " + Hex.toHex(k.getEncryptedKey()));
            }

            ask(sessions.get(senderKeyGroup), new EncryptedSessionActor.DecryptPackage(encKey), new AskCallback() {
                @Override
                public void onResult(Object obj) {
                    Log.d(TAG, "Decryption with key group:onResult");
                    future.onResult();
                }

                @Override
                public void onError(Exception e) {
                    Log.d(TAG, "Decryption with key group:onError");
                    future.onError(e);
                }
            });
        } else {
            Log.w(TAG, "Unable to find appropriate session #" + senderKeyGroup);
            future.onError(new RuntimeException());
        }
    }

    @Override
    public boolean onAsk(Object message, Future future) {
        if (message instanceof EncryptPackage) {
            doEncrypt(((EncryptPackage) message).getData(), future);
            return false;
        } else if (message instanceof DecryptPackage) {
            doDecrypt(((DecryptPackage) message).getEncryptedBox(), future);
            return false;
        } else {
            return super.onAsk(message, future);
        }
    }

    @Override
    public void onReceive(Object message) {
        Log.d(TAG, "msg: " + message);
        super.onReceive(message);
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

    public static class DecryptPackage {

        private EncryptedBox encryptedBox;

        public DecryptPackage(EncryptedBox encryptedBox) {
            this.encryptedBox = encryptedBox;
        }

        public EncryptedBox getEncryptedBox() {
            return encryptedBox;
        }
    }
}
