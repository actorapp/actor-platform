package im.actor.runtime.crypto;

import im.actor.runtime.crypto.primitives.util.ByteStrings;

// Disabling Bounds checks for speeding up calculations

/*-[
#define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
]-*/

/**
 * Actor's MTProto V2 keys
 */
public class ActorProtoKey {

    private byte[] clientMacKey;
    private byte[] serverMacKey;
    private byte[] clientKey;
    private byte[] serverKey;

    private byte[] clientMacRussianKey;
    private byte[] serverMacRussianKey;
    private byte[] clientRussianKey;
    private byte[] serverRussianKey;

    public ActorProtoKey(byte[] masterKey) {
        int offset = 0;
        clientMacKey = ByteStrings.substring(masterKey, (offset++) * 32, 32);
        serverMacKey = ByteStrings.substring(masterKey, (offset++) * 32, 32);
        clientKey = ByteStrings.substring(masterKey, (offset++) * 32, 32);
        serverKey = ByteStrings.substring(masterKey, (offset++) * 32, 32);

        clientMacRussianKey = ByteStrings.substring(masterKey, (offset++) * 32, 32);
        serverMacRussianKey = ByteStrings.substring(masterKey, (offset++) * 32, 32);
        clientRussianKey = ByteStrings.substring(masterKey, (offset++) * 32, 32);
        serverRussianKey = ByteStrings.substring(masterKey, (offset++) * 32, 32);
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

    public byte[] getClientMacRussianKey() {
        return clientMacRussianKey;
    }

    public byte[] getServerMacRussianKey() {
        return serverMacRussianKey;
    }

    public byte[] getClientRussianKey() {
        return clientRussianKey;
    }

    public byte[] getServerRussianKey() {
        return serverRussianKey;
    }
}