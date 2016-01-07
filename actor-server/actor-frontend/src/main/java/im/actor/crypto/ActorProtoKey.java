package im.actor.crypto;

import im.actor.crypto.primitives.ByteStrings;

/**
 * Actor's MTProto V2 keys
 */
public class ActorProtoKey {

    private byte[] clientMacKey;
    private byte[] serverMacKey;
    private byte[] clientKey;
    private byte[] serverKey;

    public ActorProtoKey(byte[] masterKey) {
        clientMacKey = ByteStrings.substring(masterKey, 0, 32);
        serverMacKey = ByteStrings.substring(masterKey, 32, 32);
        clientKey = ByteStrings.substring(masterKey, 64, 32);
        serverKey = ByteStrings.substring(masterKey, 96, 32);
    }

    public byte[] getClientMacKey() {
        return clientMacKey;
    }

    public byte[] getServerMacKey() {
        return serverMacKey;
    }

    public byte[] getClientKey() {
        return clientKey;
    }

    public byte[] getServerKey() {
        return serverKey;
    }
}