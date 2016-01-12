package im.actor.core.api.rpc;
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

import im.actor.core.api.*;

public class RequestDisableFeature extends Request<ResponseVoid> {

    public static final int HEADER = 0xa1d;

    public static RequestDisableFeature fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestDisableFeature(), data);
    }

    private String featureName;

    public RequestDisableFeature(@NotNull String featureName) {
        this.featureName = featureName;
    }

    public RequestDisableFeature() {

    }

    @NotNull
    public String getFeatureName() {
        return this.featureName;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.featureName = values.getString(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.featureName == null) {
            throw new IOException();
        }
        writer.writeString(1, this.featureName);
    }

    @Override
    public String toString() {
        String res = "rpc DisableFeature{";
        res += "featureName=" + this.featureName;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
