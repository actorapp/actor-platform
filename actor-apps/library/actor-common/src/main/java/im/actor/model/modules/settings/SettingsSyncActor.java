/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.settings;

import java.io.IOException;

import im.actor.model.api.Parameter;
import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.rpc.RequestEditParameter;
import im.actor.model.api.rpc.RequestGetParameters;
import im.actor.model.api.rpc.ResponseGetParameters;
import im.actor.model.api.rpc.ResponseSeq;
import im.actor.model.api.updates.UpdateParameterChanged;
import im.actor.model.modules.Modules;
import im.actor.model.modules.settings.entity.SettingsSyncAction;
import im.actor.model.modules.settings.entity.SettingsSyncState;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

public class SettingsSyncActor extends ModuleActor {

    private static final String SYNC_STATE = "settings_sync_state";
    private static final String SYNC_STATE_LOADED = "settings_sync_state_loaded";

    private SettingsSyncState syncState;

    public SettingsSyncActor(Modules modules) {
        super(modules);
    }

    @Override
    public void preStart() {
        super.preStart();
        syncState = new SettingsSyncState();
        byte[] data = preferences().getBytes(SYNC_STATE);
        if (data != null) {
            try {
                syncState = SettingsSyncState.fromBytes(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (SettingsSyncAction action : syncState.getPendingActions()) {
            performSync(action);
        }

        if (!preferences().getBool(SYNC_STATE_LOADED, false)) {
            // TODO: Avoid race conditions
            request(new RequestGetParameters(), new RpcCallback<ResponseGetParameters>() {
                @Override
                public void onResult(ResponseGetParameters response) {
                    for (Parameter p : response.getParameters()) {
                        modules().getPreferences().putString(p.getKey(), p.getValue());
                    }
                    preferences().putBool(SYNC_STATE_LOADED, true);
                }

                @Override
                public void onError(RpcException e) {
                    // Ignore
                }
            });
        }
    }

    private void performSync(final SettingsSyncAction action) {
        request(new RequestEditParameter(action.getKey(), action.getValue()), new RpcCallback<ResponseSeq>() {
            @Override
            public void onResult(ResponseSeq response) {
                syncState.getPendingActions().remove(action);
                saveState();
                updates().onUpdateReceived(new SeqUpdate(response.getSeq(), response.getState(),
                        UpdateParameterChanged.HEADER, new UpdateParameterChanged(action.getKey(), action.getValue()).toByteArray()));
            }

            @Override
            public void onError(RpcException e) {
                // Ignore
            }
        });
    }

    private void saveState() {
        preferences().putBytes(SYNC_STATE, syncState.toByteArray());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof ChangeSettings) {
            ChangeSettings changeSettings = (ChangeSettings) message;
            SettingsSyncAction action = new SettingsSyncAction(changeSettings.getKey(),
                    changeSettings.getValue());
            syncState.getPendingActions().add(action);
            saveState();
            performSync(action);
        } else {
            drop(message);
        }
    }

    public static class ChangeSettings {
        private String key;
        private String value;

        public ChangeSettings(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}