/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.notifications.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class PendingStorage extends BserObject {

    public static PendingStorage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new PendingStorage(), data);
    }

    private List<PendingNotification> notifications;

    public PendingStorage() {
        notifications = new ArrayList<PendingNotification>();
    }

    public List<PendingNotification> getNotifications() {
        return notifications;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        int count = values.getRepeatedCount(1);
        if (count > 0) {
            ArrayList<PendingNotification> stubs = new ArrayList<PendingNotification>();
            for (int i = 0; i < count; i++) {
                stubs.add(new PendingNotification());

            }
            notifications = values.getRepeatedObj(1, stubs);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, notifications);
    }
}
