package im.actor.model.api;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.bser.util.SparseArray;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class JsonMessage extends Message {

    private String rawJson;

    public JsonMessage(@NotNull String rawJson) {
        this.rawJson = rawJson;
    }

    public JsonMessage() {

    }

    public int getHeader() {
        return 4;
    }

    @NotNull
    public String getRawJson() {
        return this.rawJson;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.rawJson = values.getString(1);
        if (values.hasRemaining()) {
            setUnmappedObjects(values.buildRemaining());
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.rawJson == null) {
            throw new IOException();
        }
        writer.writeString(1, this.rawJson);
        if (this.getUnmappedObjects() != null) {
            SparseArray<Object> unmapped = this.getUnmappedObjects();
            for (int i = 0; i < unmapped.size(); i++) {
                int key = unmapped.keyAt(i);
                writer.writeUnmapped(key, unmapped.get(key));
            }
        }
    }

    @Override
    public String toString() {
        String res = "struct JsonMessage{";
        res += "}";
        return res;
    }

}
