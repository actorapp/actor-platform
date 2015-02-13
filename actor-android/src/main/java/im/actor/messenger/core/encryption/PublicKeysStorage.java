package im.actor.messenger.core.encryption;

import java.util.ArrayList;

import im.actor.messenger.storage.scheme.users.User;

import static im.actor.messenger.storage.KeyValueEngines.publicKeys;

public class PublicKeysStorage {

    public static KeyLoadResult getUserPublicKeys(final User user, long exclude) {
        int totalKeysCount = user.getKeyHashes().size();
        ArrayList<im.actor.messenger.storage.scheme.users.PublicKey> keys = new ArrayList<im.actor.messenger.storage.scheme.users.PublicKey>(totalKeysCount);
        ArrayList<Long> missing = new ArrayList<Long>();
        for (Long keyHash : user.getKeyHashes()) {
            if (keyHash == exclude) {
                continue;
            }
            im.actor.messenger.storage.scheme.users.PublicKey key = publicKeys().get(keyHash);
            if (key != null) {
                keys.add(key);
            } else {
                missing.add(keyHash);
            }
        }

        return new KeyLoadResult(missing, keys);
    }

    public static class KeyLoadResult {
        private ArrayList<Long> missing;
        private ArrayList<im.actor.messenger.storage.scheme.users.PublicKey> keys;

        public KeyLoadResult(ArrayList<Long> missing, ArrayList<im.actor.messenger.storage.scheme.users.PublicKey> keys) {
            this.missing = missing;
            this.keys = keys;
        }

        public ArrayList<Long> getMissing() {
            return missing;
        }

        public ArrayList<im.actor.messenger.storage.scheme.users.PublicKey> getKeys() {
            return keys;
        }
    }
}
