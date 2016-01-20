package im.actor.core.api;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.runtime.collections.*;
import static im.actor.runtime.bser.Utils.*;
import im.actor.core.network.parser.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.j2objc.annotations.ObjectiveCName;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ApiEncyptedBoxKey extends BserObject {

    private int usersId;
    private int keyGroupId;
    private String algType;
    private byte[] encryptedKey;

    public ApiEncyptedBoxKey(int usersId, int keyGroupId, @NotNull String algType, @NotNull byte[] encryptedKey) {
        this.usersId = usersId;
        this.keyGroupId = keyGroupId;
        this.algType = algType;
        this.encryptedKey = encryptedKey;
    }

    public ApiEncyptedBoxKey() {

    }

    public int getUsersId() {
        return this.usersId;
    }

    public int getKeyGroupId() {
        return this.keyGroupId;
    }

    @NotNull
    public String getAlgType() {
        return this.algType;
    }

    @NotNull
    public byte[] getEncryptedKey() {
        return this.encryptedKey;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.usersId = values.getInt(1);
        this.keyGroupId = values.getInt(2);
        this.algType = values.getString(3);
        this.encryptedKey = values.getBytes(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, this.usersId);
        writer.writeInt(2, this.keyGroupId);
        if (this.algType == null) {
            throw new IOException();
        }
        writer.writeString(3, this.algType);
        if (this.encryptedKey == null) {
            throw new IOException();
        }
        writer.writeBytes(4, this.encryptedKey);
    }

    @Override
    public String toString() {
        String res = "struct EncyptedBoxKey{";
        res += "keyGroupId=" + this.keyGroupId;
        res += ", algType=" + this.algType;
        res += "}";
        return res;
    }

}
