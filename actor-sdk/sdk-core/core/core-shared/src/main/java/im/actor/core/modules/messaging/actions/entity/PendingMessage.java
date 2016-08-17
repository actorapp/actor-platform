/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.actions.entity;

import java.io.IOException;

import im.actor.core.modules.file.entity.EncryptionInfo;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;

public class PendingMessage extends BserObject {

    public static PendingMessage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new PendingMessage(), data);
    }

    private Peer peer;
    private long rid;
    private AbsContent content;
    private boolean isError;
    private int timer;
    private EncryptionInfo encryptionInfo;

    public PendingMessage(Peer peer, long rid, AbsContent content, int timer, EncryptionInfo encryptionInfo) {
        this.peer = peer;
        this.rid = rid;
        this.content = content;
        this.timer = timer;
        this.encryptionInfo = encryptionInfo;
    }

    private PendingMessage() {

    }

    public Peer getPeer() {
        return peer;
    }

    public AbsContent getContent() {
        return content;
    }

    public long getRid() {
        return rid;
    }

    public boolean isError() {
        return isError;
    }

    public int getTimer() {
        return timer;
    }

    public EncryptionInfo getEncryptionInfo() {
        return encryptionInfo;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromUniqueId(values.getLong(1));
        rid = values.getLong(2);
        content = AbsContent.parse(values.getBytes(3));
        isError = values.getBool(4, false);
        timer = values.getInt(5, 0);
        byte[] dt = values.getBytes(6);
        if (dt != null) {
            encryptionInfo = EncryptionInfo.fromBytes(dt);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, peer.getUnuqueId());
        writer.writeLong(2, rid);
        writer.writeBytes(3, AbsContent.serialize(content));
        writer.writeBool(4, isError);
        writer.writeInt(5, timer);
        if (encryptionInfo != null) {
            writer.writeObject(6, encryptionInfo);
        }
    }
}
