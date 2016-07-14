package im.actor.core.modules.encryption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedContent;
import im.actor.core.api.ApiEncryptedMessageContent;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestSendEncryptedPackage;
import im.actor.core.api.rpc.ResponseSendEncryptedPackage;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.ratchet.EncryptedMsg;
import im.actor.core.modules.encryption.ratchet.EncryptedUser;
import im.actor.core.modules.encryption.ratchet.EncryptedUserActor;
import im.actor.core.modules.encryption.ratchet.KeyManager;
import im.actor.core.modules.encryption.ratchet.SessionManager;
import im.actor.core.modules.encryption.ratchet.entity.EncryptedMessage;
import im.actor.runtime.function.Function;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptionModule extends AbsModule {

    private KeyManager keyManager;
    private SessionManager sessionManager;
    private EncryptedMsg encryption;

    private final HashMap<Integer, EncryptedUser> users = new HashMap<>();

    public EncryptionModule(ModuleContext context) {
        super(context);
    }

    public void run() {
        keyManager = new KeyManager(context());
        sessionManager = new SessionManager(context());
        encryption = new EncryptedMsg(context());
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public EncryptedMsg getEncryption() {
        return encryption;
    }

    public EncryptedUser getEncryptedUser(int uid) {
        synchronized (users) {
            if (!users.containsKey(uid)) {
                users.put(uid, new EncryptedUser(system().actorOf("encryption/uid_" + uid,
                        () -> new EncryptedUserActor(uid, context()))));
            }
            return users.get(uid);
        }
    }

    public Promise<EncryptedMessage> encrypt(List<Integer> uids, ApiEncryptedContent message) {
        return getEncryption().encrypt(uids, message);
    }

    public Promise<ApiEncryptedContent> decrypt(int uid, ApiEncryptedBox encryptedBox) {
        return getEncryption().decrypt(uid, encryptedBox);
    }

    public Promise<ResponseSendEncryptedPackage> doSend(long rid, ApiEncryptedContent content, int uid) {
        ArrayList<Integer> receiver = new ArrayList<>();
        receiver.add(uid);
//        if (uid != myUid()) {
//            receiver.add(myUid());
//        }
        return doSend(rid, content, receiver);
    }

    public Promise<ResponseSendEncryptedPackage> doSend(long rid, ApiEncryptedContent content, List<Integer> uids) {

        ArrayList<ApiUserOutPeer> outPeers = new ArrayList<>();
        for (int i : uids) {
            outPeers.add(new ApiUserOutPeer(i, users().getValue(i).getAccessHash()));
        }

        return encrypt(uids, content).flatMap(encryptedMessage -> {
            RequestSendEncryptedPackage request = new RequestSendEncryptedPackage(rid, outPeers,
                    encryptedMessage.getIgnoredGroups(), encryptedMessage.getEncryptedBox());
            return api(request).flatMap(r -> {
                if (r.getSeq() != null && r.getSeq() != 0) {
                    return Promise.success(r);
                }
                return Promise.failure(new RuntimeException("Incorrect keys"));
            });
        });
    }
}
