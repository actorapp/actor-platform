/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RpcException extends Exception {

    @NotNull
    @Property("readonly, nonatomic")
    private String tag;
    @Property("readonly, nonatomic")
    private int code;
    @NotNull
    @Property("readonly, nonatomic")
    private String message;
    @Property("readonly, nonatomic")
    private boolean canTryAgain;
    @Nullable
    @Property("readonly, nonatomic")
    private byte[] relatedData;

    public RpcException(@NotNull String tag, int code, @NotNull String message, boolean canTryAgain, @Nullable byte[] relatedData) {
        this.tag = tag;
        this.code = code;
        this.message = message;
        this.canTryAgain = canTryAgain;
        this.relatedData = relatedData;
    }

    public boolean isCanTryAgain() {
        return canTryAgain;
    }

    @Nullable
    public byte[] getRelatedData() {
        return relatedData;
    }

    @NotNull
    public String getTag() {
        return tag;
    }

    public int getCode() {
        return code;
    }

    @Override
    @NotNull
    public String getMessage() {
        return message;
    }
}
