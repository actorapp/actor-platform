/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.modules;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

import im.actor.core.api.ApiFileLocation;
import im.actor.core.api.ApiFileUrlDescription;
import im.actor.core.api.rpc.RequestGetFileUrl;
import im.actor.core.api.rpc.RequestGetFileUrls;
import im.actor.core.api.rpc.ResponseGetFileUrl;
import im.actor.core.api.rpc.ResponseGetFileUrls;
import im.actor.core.js.modules.entity.CachedFileUrl;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.Modules;
import im.actor.core.modules.utils.BaseKeyValueEngine;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

/**
 * File's URL binder
 */
public class JsFilesModule extends AbsModule {

    private static final String TAG = "JsFilesModule";

    private ActorRef urlLoader;
    private BaseKeyValueEngine<CachedFileUrl> keyValueStorage;
    private HashSet<Long> requestedFiles = new HashSet<Long>();
    private ArrayList<JsFileLoadedListener> listeners = new ArrayList<JsFileLoadedListener>();

    public JsFilesModule(final Modules modules) {
        super(modules);

        urlLoader = system().actorOf(Props.create(FileBinderActor.class, new ActorCreator<FileBinderActor>() {
            @Override
            public FileBinderActor create() {
                return new FileBinderActor(JsFilesModule.this, modules);
            }
        }), "files/url_loader");

        keyValueStorage = new BaseKeyValueEngine<CachedFileUrl>(Storage.createKeyValue("file_url_cache")) {

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

    /**
     * Registering Internal listener for binding updates
     *
     * @param listener listener for registration
     */
    public void registerListener(JsFileLoadedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregistering Internal listener for binding updates
     *
     * @param listener listener for unregistering
     */
    public void unregisterListener(JsFileLoadedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Getting URL for file if available
     *
     * @param id         file's id
     * @param accessHash file's accessHash
     * @return url for a file or null if not yet available
     */
    public String getFileUrl(long id, long accessHash) {
        CachedFileUrl cachedFileUrl = keyValueStorage.getValue(id);
        if (cachedFileUrl != null) {
            long urlTime = cachedFileUrl.getTimeout();
            long currentTime = im.actor.runtime.Runtime.getCurrentSyncedTime();
            if (urlTime <= currentTime) {
                Log.w("JsFilesModule", "URL #" + id + " timeout (urlTime: " + urlTime + ", current:" + currentTime + ")");
                keyValueStorage.removeItem(id);
            } else {
                return cachedFileUrl.getUrl();
            }
        }

        if (!requestedFiles.contains(id)) {
            requestedFiles.add(id);
            urlLoader.send(new FileRequest(id, accessHash));
        }

        return null;
    }

    private void onFileUrlLoaded(ArrayList<FileResponse> responses) {
        HashSet<Long> ids = new HashSet<Long>();
        ArrayList<CachedFileUrl> cachedFileUrls = new ArrayList<CachedFileUrl>();
        for (FileResponse r : responses) {
            ids.add(r.getId());
            requestedFiles.remove(r.getId());
            cachedFileUrls.add(new CachedFileUrl(r.getId(), r.getUrl(), r.getTimeout()));
        }
        keyValueStorage.addOrUpdateItems(cachedFileUrls);
        for (JsFileLoadedListener listener : listeners) {
            listener.onFileLoaded(ids);
        }
    }

    /**
     * Internal File Url loader
     */
    private static class FileBinderActor extends ModuleActor {
        private static final long DELAY = 200;
        private static final int MAX_FILE_SIZE = 50;

        private boolean isLoading = false;
        private JsFilesModule filesModule;
        private ArrayList<FileRequest> filesQueue = new ArrayList<FileRequest>();

        public FileBinderActor(JsFilesModule filesModule, ModuleContext context) {
            super(context);

            this.filesModule = filesModule;
        }

        @Override
        public void onReceive(Object message) {
            if (message instanceof FileRequest) {
                filesQueue.add((FileRequest) message);
                self().sendOnce(new PerformLoad(), DELAY);
            } else if (message instanceof PerformLoad) {
                performLoad();
            } else {
                drop(message);
            }
        }

        private void performLoad() {
            if (isLoading) {
                return;
            }
            ArrayList<ApiFileLocation> fileLocations = new ArrayList<ApiFileLocation>();
            for (int i = 0; i < MAX_FILE_SIZE && filesQueue.size() > 0; i++) {
                FileRequest request = filesQueue.remove(0);
                fileLocations.add(new ApiFileLocation(request.getId(),
                        request.getAccessHash()));
            }
            if (fileLocations.size() == 0) {
                return;
            }

            isLoading = true;
            request(new RequestGetFileUrls(fileLocations), new RpcCallback<ResponseGetFileUrls>() {
                @Override
                public void onResult(ResponseGetFileUrls response) {

                    // Converting result
                    long currentTime = im.actor.runtime.Runtime.getCurrentSyncedTime();
                    ArrayList<FileResponse> responses = new ArrayList<FileResponse>();
                    for (ApiFileUrlDescription u : response.getFileUrls()) {
                        long urlTime = currentTime + u.getTimeout() * 1000L;
                        responses.add(new FileResponse(u.getFileId(), u.getUrl(),
                                urlTime));
                    }

                    // Notify about loaded
                    filesModule.onFileUrlLoaded(responses);

                    isLoading = false;
                    self().sendOnce(new PerformLoad(), DELAY);
                }

                @Override
                public void onError(RpcException e) {
                    // Setting flag
                    isLoading = false;

                    // Logging error
                    Log.e(TAG, e);

                    isLoading = false;
                    self().sendOnce(new PerformLoad(), DELAY);
                }
            });
        }

        private class PerformLoad {

        }
    }

    private static class FileRequest {

        private long id;
        private long accessHash;

        public FileRequest(long id, long accessHash) {
            this.id = id;
            this.accessHash = accessHash;
        }

        public long getId() {
            return id;
        }

        public long getAccessHash() {
            return accessHash;
        }
    }

    private static class FileResponse {
        private long id;
        private String url;
        private long timeout;

        public FileResponse(long id, String url, long timeout) {
            this.id = id;
            this.url = url;
            this.timeout = timeout;
        }

        public long getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public long getTimeout() {
            return timeout;
        }
    }
}