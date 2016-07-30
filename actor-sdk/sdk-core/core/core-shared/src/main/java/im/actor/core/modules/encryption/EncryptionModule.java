package im.actor.core.modules.encryption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedChatTimerSet;
import im.actor.core.api.ApiEncryptedContent;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestSendEncryptedPackage;
import im.actor.core.api.rpc.ResponseSendEncryptedPackage;
import im.actor.core.entity.EncryptedConversationState;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.ratchet.EncryptedMsg;
import im.actor.core.modules.encryption.ratchet.EncryptedUser;
import im.actor.core.modules.encryption.ratchet.EncryptedUserActor;
import im.actor.core.modules.encryption.ratchet.KeyManager;
import im.actor.core.modules.encryption.ratchet.SessionManager;
import im.actor.core.modules.encryption.ratchet.entity.EncryptedMessage;
import im.actor.core.util.RandomUtils;
import im.actor.core.viewmodel.EncryptedConversationVM;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.KeyValueStorage;

import static im.actor.runtime.actors.ActorSystem.system;

public class EncryptionModule extends AbsModule {

    // j2objc workaround
    private static final Void DUMB = null;

    private KeyManager keyManager;
    private SessionManager sessionManager;
    private EncryptedRouter encryptedRouter;
    private EncryptedMsg encryption;
    private KeyValueStorage keyValueStorage;
    private MVVMCollection<EncryptedConversationState, EncryptedConversationVM> conversationState;
    private final HashMap<Integer, EncryptedUser> users = new HashMap<>();

    public EncryptionModule(ModuleContext context) {
        super(context);
    }

    public void run() {

        keyValueStorage = Storage.createKeyValue("session_temp_storage");
        conversationState = Storage.createKeyValue("encrypted_chat_state", EncryptedConversationVM.CREATOR,
                EncryptedConversationState.CREATOR, EncryptedConversationState.DEFAULT_CREATOR);

        keyManager = new KeyManager(context());
        sessionManager = new SessionManager(context());
        encryption = new EncryptedMsg(context());
        encryptedRouter = new EncryptedRouter(context());
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public EncryptedRouter getRouter() {
        return encryptedRouter;
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

    public KeyValueStorage getKeyValueStorage() {
        return keyValueStorage;
    }

    public MVVMCollection<EncryptedConversationState, EncryptedConversationVM> getConversationState() {
        return conversationState;
    }

    public Promise<Void> onUpdate(int senderId, long date, ApiEncryptedContent update) {
        return encryptedRouter.onEncryptedUpdate(senderId, date, update);
    }

    public Promise<Long> doSend(ApiEncryptedContent content, int uid) {
        return doSend(RandomUtils.nextRid(), content, uid);
    }

    public Promise<Long> doSend(ApiEncryptedContent content, List<Integer> uids) {
        return doSend(RandomUtils.nextRid(), content, uids);
    }

    public Promise<Long> doSend(long rid, ApiEncryptedContent content, int uid) {
        ArrayList<Integer> receiver = new ArrayList<>();
        receiver.add(uid);
        if (uid != myUid()) {
            receiver.add(myUid());
        }
        return doSend(rid, content, receiver);
    }

    public Promise<Long> doSend(long rid, ApiEncryptedContent content, List<Integer> uids) {

        ArrayList<ApiUserOutPeer> outPeers = new ArrayList<>();
        for (int i : uids) {
            outPeers.add(new ApiUserOutPeer(i, users().getValue(i).getAccessHash()));
        }

        return getEncryption().encrypt(uids, content)
                .flatMap(m -> api(new RequestSendEncryptedPackage(rid, outPeers, m.getIgnoredGroups(), m.getEncryptedBox())))
                .flatMap(r -> {
                    if (r.getDate() != null) {
                        return Promise.success(r.getDate());
                    } else {
                        return getKeyManager().onKeyGroupDiffReceived(r.getMissedKeyGroups(), r.getObsoleteKeyGroups())
                                .flatMap(r2 -> doSend(rid, content, uids));
                    }
                });
    }


    public Promise<Void> setSecretChatTimer(int uid, Integer timeout) {
        ApiEncryptedContent encryptedContent = new ApiEncryptedChatTimerSet(uid,
                RandomUtils.nextRid(), timeout);
        return doSend(encryptedContent, uid)
                .flatMap(r -> getRouter().onEncryptedUpdate(myUid(), r, encryptedContent));
    }
}
