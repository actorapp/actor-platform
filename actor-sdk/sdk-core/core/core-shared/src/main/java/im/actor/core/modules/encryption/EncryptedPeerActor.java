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
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
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

public class EncryptedPeerActor extends ModuleActor {

    private static final String TAG = "EncryptedStateActor";

    private int uid;
    private ArrayList<ApiEncryptionKeyGroup> keyGroups;
    private HashMap<Integer, ActorRef> sessions = new HashMap<Integer, ActorRef>();
    private boolean isReady = false;

    public EncryptedPeerActor(int uid, ModuleContext context) {
        super(context);
        this.uid = uid;
    }

    @Override
    public void preStart() {
        super.preStart();

        if (keyGroups == null) {
            Log.d(TAG, "Loading own keys for conversation");
            User user = getUser(uid);
            request(new RequestLoadPublicKeyGroups(new ApiUserOutPeer(user.getUid(), user.getAccessHash())), new RpcCallback<ResponsePublicKeyGroups>() {
                @Override
                public void onResult(ResponsePublicKeyGroups response) {
                    keyGroups = new ArrayList<ApiEncryptionKeyGroup>(response.getPublicKeyGroups());
                    onGroupsReady();
                }

                @Override
                public void onError(RpcException e) {
                    Log.d(TAG, "Error during loading public key groups");
                    Log.e(TAG, e);

                    // Do nothing
                }
            });
        } else {
            onGroupsReady();
        }
    }

    private void onGroupsReady() {
        Log.w(TAG, "Groups ready #" + keyGroups.size());

        for (final ApiEncryptionKeyGroup g : keyGroups) {
            sessions.put(g.getKeyGroupId(), system().actorOf(Props.create(EncryptionSessionActor.class, new ActorCreator<EncryptionSessionActor>() {
                @Override
                public EncryptionSessionActor create() {
                    return new EncryptionSessionActor(context(), uid, g);
                }
            }), getPath() + "/k_" + g.getKeyGroupId()));
        }
        isReady = true;
        unstashAll();
    }

    private void doEncrypt(final byte[] data, final Future future) {

        Log.d(TAG, "doEncrypt");

        final byte[] encKey = Crypto.randomBytes(128);

        final ArrayList<EncryptedBoxKey> encryptedKeys = new ArrayList<EncryptedBoxKey>();
        for (final Integer keyGroup : sessions.keySet()) {
            ask(sessions.get(keyGroup), new EncryptionSessionActor.EncryptPackage(encKey), new AskCallback() {
                @Override
                public void onResult(Object obj) {
                    EncryptionSessionActor.EncryptedPackageRes res = (EncryptionSessionActor.EncryptedPackageRes) obj;
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
            encData = ActorBox.closeBox(new byte[0], data, Crypto.randomBytes(32), new ActorBoxKey(encKey));
        } catch (IntegrityException e) {
            e.printStackTrace();
            future.onError(e);
            return;
        }

        EncryptedBox encryptedBox = new EncryptedBox(encryptedKeys.toArray(new EncryptedBoxKey[0]), encData);
        future.onResult(encryptedBox);
    }

    private void doDecrypt(EncryptedBox data, Future future) {

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
        if (!isReady && message instanceof AskRequest) {
            stash();
            return;
        }
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
