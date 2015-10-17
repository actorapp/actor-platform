/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.settings.entity;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class SettingsSyncAction extends BserObject {

    public static SettingsSyncAction fromBytes(byte[] data) throws IOException {
        return Bser.parse(new SettingsSyncAction(), data);
    }

    private String key;
    private String value;

    public SettingsSyncAction(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private SettingsSyncAction() {

    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        key = values.getString(1);
        value = values.optString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeString(1, key);
        if (value != null) {
            writer.writeString(2, value);
        }
    }
}
