package im.actor.core.modules.internal.encryption;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestLoadPublicKeyGroups;
import im.actor.core.api.rpc.ResponsePublicKeyGroups;
import im.actor.core.entity.User;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.encryption.entity.EncryptedBox;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Future;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.ask.AskRequest;

public class EncryptedStateActor extends ModuleActor {

    private static final String TAG = "EncryptedStateActor";

    private int uid;
    private ArrayList<ApiEncryptionKeyGroup> keyGroups;
    private HashMap<Integer, ActorRef> sessions = new HashMap<Integer, ActorRef>();
    private boolean isReady = false;

    public EncryptedStateActor(int uid, ModuleContext context) {
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
    }

    private void doEncrypt(byte[] data, Future future) {

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
