package im.actor.core.modules.encryption;

import java.util.ArrayList;

import im.actor.runtime.collections.ManagedList;

public final class Configuration {

    public static final int EPHEMERAL_KEYS_COUNT = 100;

    public static final ArrayList<String> SUPPORTED = ManagedList.of(
            "curve25519",
            "Ed25519",
            "kuznechik128",
            "streebog256",
            "sha256",
            "sha512",
            "aes128"
    );

    private Configuration() {

    }
}
