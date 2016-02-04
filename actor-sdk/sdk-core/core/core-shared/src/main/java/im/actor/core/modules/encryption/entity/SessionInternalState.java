package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class SessionInternalState extends BserObject {

    private List<byte[]> ownPrivateKeys = new ArrayList<>();
    private byte[] theirLastPublicKey;
    private boolean isRatcheting;

    public SessionInternalState(boolean isRatcheting, List<byte[]> ownPrivateKeys, byte[] theirLastPublicKey) {
        this.ownPrivateKeys = ownPrivateKeys;
        this.theirLastPublicKey = theirLastPublicKey;
        this.isRatcheting = isRatcheting;
    }

    public SessionInternalState(byte[] state) throws IOException {
        load(state);
    }

    public List<byte[]> getOwnPrivateKeys() {
        return ownPrivateKeys;
    }

    public byte[] getTheirLastPublicKey() {
        return theirLastPublicKey;
    }

    public boolean isRatcheting() {
        return isRatcheting;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.ownPrivateKeys = values.getRepeatedBytes(1);
        this.theirLastPublicKey = values.optBytes(2);
        this.isRatcheting = values.getBool(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        for (byte[] ownKey : ownPrivateKeys) {
            writer.writeBytes(1, ownKey);
        }
        if (theirLastPublicKey != null) {
            writer.writeBytes(2, theirLastPublicKey);
        }
        writer.writeBool(3, isRatcheting);
    }
}