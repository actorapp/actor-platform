/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.angular;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import im.actor.model.api.FileLocation;
import im.actor.model.api.rpc.RequestGetFileUrl;
import im.actor.model.api.rpc.ResponseGetFileUrl;
import im.actor.model.droidkit.actors.Environment;
import im.actor.model.js.angular.entity.CachedFileUrl;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.BaseKeyValueEngine;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class AngularFilesModule extends BaseModule {

    private static final String TAG = "AngularFilesModule";

    private BaseKeyValueEngine<CachedFileUrl> keyValueStorage;
    private HashSet<Long> requestedFiles = new HashSet<Long>();
    private ArrayList<AngularFileLoadedListener> listeners = new ArrayList<AngularFileLoadedListener>();

    public AngularFilesModule(Modules modules) {
        super(modules);

        keyValueStorage = new BaseKeyValueEngine<CachedFileUrl>(
                modules.getConfiguration()
                        .getStorageProvider()
                        .createKeyValue("file_url_cache")) {

            @Override
            protected byte[] serialize(CachedFileUrl value) {
                return value.toByteArray();
            }

            @Override
            protected CachedFileUrl deserialize(byte[] data) {
                try {
                    return CachedFileUrl.fromBytes(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    public void registerListener(AngularFileLoadedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(AngularFileLoadedListener listener) {
        listeners.remove(listener);
    }

    public String getFileUrl(long id, long accessHash) {
        CachedFileUrl cachedFileUrl = keyValueStorage.getValue(id);
        if (cachedFileUrl != null) {
            if (cachedFileUrl.getTimeout() <= Environment.getCurrentSyncedTime()) {
                keyValueStorage.removeItem(id);
            } else {
                return cachedFileUrl.getUrl();
            }
        }
        requestFileUrl(id, accessHash);
        return null;
    }

    public void requestFileUrl(final long id, long accessHash) {
        if (requestedFiles.contains(id)) {
            return;
        }
        requestedFiles.add(id);

        request(new RequestGetFileUrl(new FileLocation(id, accessHash)), new RpcCallback<ResponseGetFileUrl>() {
            @Override
            public void onResult(ResponseGetFileUrl response) {
                requestedFiles.remove(id);
                keyValueStorage.addOrUpdateItem(new CachedFileUrl(id, response.getUrl(),
                        Environment.getCurrentSyncedTime() + response.getTimeout() * 1000L));
                for (AngularFileLoadedListener listener : listeners) {
                    listener.onFileLoaded(id);
                }
            }

            @Override
            public void onError(RpcException e) {
                requestedFiles.remove(id);
            }
        });
    }
}