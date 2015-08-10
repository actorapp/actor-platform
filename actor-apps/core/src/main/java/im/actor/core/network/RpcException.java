/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network;

public class RpcException extends Exception {
    private String tag;
    private int code;
    private String message;
    private boolean canTryAgain;
    private byte[] relatedData;

    public RpcException(String tag, int code, String message, boolean canTryAgain, byte[] relatedData) {
        this.tag = tag;
        this.code = code;
        this.message = message;
        this.canTryAgain = canTryAgain;
        this.relatedData = relatedData;
    }

    public boolean isCanTryAgain() {
        return canTryAgain;
    }

    public byte[] getRelatedData() {
        return relatedData;
    }

    public String getTag() {
        return tag;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
