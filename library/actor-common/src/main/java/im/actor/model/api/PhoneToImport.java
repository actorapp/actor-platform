package im.actor.model.api;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;
import im.actor.model.droidkit.bser.util.SparseArray;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.j2objc.annotations.ObjectiveCName;
import static im.actor.model.droidkit.bser.Utils.*;
import java.io.IOException;
import im.actor.model.network.parser.*;
import java.util.List;
import java.util.ArrayList;

public class PhoneToImport extends BserObject {

    private long phoneNumber;
    private String name;

    public PhoneToImport(long phoneNumber, @Nullable String name) {
        this.phoneNumber = phoneNumber;
        this.name = name;
    }

    public PhoneToImport() {

    }

    public long getPhoneNumber() {
        return this.phoneNumber;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.phoneNumber = values.getLong(1);
        this.name = values.optString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, this.phoneNumber);
        if (this.name != null) {
            writer.writeString(2, this.name);
        }
    }

    @Override
    public String toString() {
        String res = "struct PhoneToImport{";
        res += "phoneNumber=" + this.phoneNumber;
        res += ", name=" + this.name;
        res += "}";
        return res;
    }

}
