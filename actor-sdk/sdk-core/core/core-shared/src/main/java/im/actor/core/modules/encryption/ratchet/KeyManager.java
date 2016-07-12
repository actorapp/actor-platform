package im.actor.core.modules.encryption.ratchet;

import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.OwnIdentity;
import im.actor.core.modules.encryption.entity.PrivateKey;
import im.actor.core.modules.encryption.entity.PublicKey;
import im.actor.core.modules.encryption.entity.UserKeys;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

import static im.actor.runtime.actors.ActorSystem.system;

/**
 * Encryption Key Manager. Used for loading user's keys for encryption/decryption.
 */
public class KeyManager extends ActorInterface {

    /**
     * Default Constructor
     *
     * @param context actor context
     */
    public KeyManager(ModuleContext context) {
        super(system().actorOf("encryption/keys", () -> new KeyManagerActor(context)));
    }


    //
    // Identity
    //

    /**
     * Loading Own Identity Key
     *
     * @return promise of keys
     */
    public Promise<OwnIdentity> getOwnIdentity() {
        return ask(new KeyManagerActor.FetchOwnKey());
    }

    /**
     * Loading user key groups by uid
     *
     * @param uid user's id
     * @return promise of key groups
     */
    public Promise<UserKeys> getUserKeyGroups(int uid) {
        return ask(new KeyManagerActor.FetchUserKeys(uid));
    }


    //
    // Pre Keys
    //

    /**
     * Load own random pre key
     *
     * @return promise of private key
     */
    public Promise<PrivateKey> getOwnRandomPreKey() {
        return ask(new KeyManagerActor.FetchOwnRandomPreKey());
    }

    /**
     * Load own pre key by key id
     *
     * @param id key id
     * @return promise of private key
     */
    public Promise<PrivateKey> getOwnPreKey(long id) {
        return ask(new KeyManagerActor.FetchOwnPreKeyById(id));
    }

    /**
     * Load own pre key by public key
     *
     * @param publicKey public key
     * @return promise of private key
     */
    public Promise<PrivateKey> getOwnPreKey(byte[] publicKey) {
        return ask(new KeyManagerActor.FetchOwnPreKeyByPublic(publicKey));
    }

    /**
     * Loading random user's pre key from key group
     *
     * @param uid        user's id
     * @param keyGroupId key group id
     * @return promise of public key
     */
    public Promise<PublicKey> getUserRandomPreKey(int uid, int keyGroupId) {
        return ask(new KeyManagerActor.FetchUserPreKeyRandom(uid, keyGroupId));
    }

    /**
     * Loading user's pre key by pre key id
     *
     * @param uid        user's id
     * @param keyGroupId key group id
     * @param preKeyId   pre key id
     * @return promise of public key
     */
    public Promise<PublicKey> getUserPreKey(int uid, int keyGroupId, long preKeyId) {
        return ask(new KeyManagerActor.FetchUserPreKey(uid, keyGroupId, preKeyId));
    }

    //
    // Updates
    //

    /**
     * Call this when update about new key group added received
     *
     * @param uid      user's id
     * @param keyGroup added key group
     * @return promise of void
     */
    public Promise<Void> onKeyGroupAdded(int uid, ApiEncryptionKeyGroup keyGroup) {
        return ask(new KeyManagerActor.PublicKeysGroupAdded(uid, keyGroup));
    }

    /**
     * Call this when update about key group removing received
     *
     * @param uid user's id
     * @param gid removed key group id
     * @return promise of void
     */
    public Promise<Void> onKeyGroupRemoved(int uid, int gid) {
        return ask(new KeyManagerActor.PublicKeysGroupRemoved(uid, gid));
    }
}
