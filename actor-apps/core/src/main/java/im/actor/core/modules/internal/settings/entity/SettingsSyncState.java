/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.settings.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class SettingsSyncState extends BserObject {

    public static SettingsSyncState fromBytes(byte[] data) throws IOException {
        return Bser.parse(new SettingsSyncState(), data);
    }

    private List<SettingsSyncAction> pendingActions = new ArrayList<SettingsSyncAction>();

    public SettingsSyncState() {
    }

    public List<SettingsSyncAction> getPendingActions() {
        return pendingActions;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        List<byte[]> pending = values.getRepeatedBytes(1);
        for (int i = 0; i < pending.size(); i++) {
            pendingActions.add(SettingsSyncAction.fromBytes(pending.get(i)));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        for (SettingsSyncAction action : pendingActions) {
            writer.writeObject(1, action);
        }
    }
}
