/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.notifications.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.modules.notifications.NotificationsQueue;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PendingStorage extends BserObject {

    private int limit;
    private int messagesCount = 0;
    private int dialogsCount = 0;

    public static PendingStorage fromBytes(byte[] data, int maxMessagesLimit) throws IOException {
        return Bser.parse(new PendingStorage(maxMessagesLimit), data);
    }

    private NotificationsQueue<PendingNotification> notifications;

    public PendingStorage(int maxMessagesLimit) {
        limit = maxMessagesLimit;
        notifications = new NotificationsQueue<>(limit);
    }

    public NotificationsQueue<PendingNotification> getNotifications() {
        return notifications;
    }

    public int getMessagesCount() {
        return messagesCount;
    }

    public void setMessagesCount(int messagesCount) {
        this.messagesCount = messagesCount;
    }

    public int getDialogsCount() {
        return dialogsCount;
    }

    public void setDialogsCount(int dialogsCount) {
        this.dialogsCount = dialogsCount;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        int count = values.getRepeatedCount(1);
        if (count > 0) {
            ArrayList<PendingNotification> stubs = new ArrayList<PendingNotification>();
            for (int i = 0; i < count; i++) {
                stubs.add(new PendingNotification());

            }
            notifications = new NotificationsQueue<>(limit).addAllChain(values.getRepeatedObj(1, stubs));
        }
        messagesCount = values.getInt(2);
        dialogsCount = values.getInt(3);

    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, notifications);
        writer.writeInt(2, messagesCount);
        writer.writeInt(3, dialogsCount);
    }
}
