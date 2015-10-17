/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.io.IOException;

import im.actor.core.entity.FileReference;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.file.DownloadManager;
import im.actor.core.modules.internal.file.UploadManager;
import im.actor.core.modules.internal.file.entity.Downloaded;
import im.actor.core.modules.utils.BaseKeyValueEngine;
import im.actor.core.viewmodel.FileCallback;
import im.actor.core.viewmodel.UploadFileCallback;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.storage.KeyValueEngine;

import static im.actor.runtime.actors.ActorSystem.system;

public class FilesModule extends AbsModule {

    private KeyValueEngine<Downloaded> downloadedEngine;
    private ActorRef downloadManager;
    private ActorRef uploadManager;

    public FilesModule(final ModuleContext context) {
        super(context);

        downloadedEngine = new BaseKeyValueEngine<Downloaded>(Storage.createKeyValue(STORAGE_DOWNLOADS)) {
            @Override
            protected byte[] serialize(Downloaded value) {
                return value.toByteArray();
            }

            @Override
            protected Downloaded deserialize(byte[] data) {
                try {
                    return Downloaded.fromBytes(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    public void run() {
        downloadManager = system().actorOf(Props.create(DownloadManager.class, new ActorCreator<DownloadManager>() {
            @Override
            public DownloadManager create() {
                return new DownloadManager(context());
            }
        }).changeDispatcher("heavy"), "actor/download/manager");
        uploadManager = system().actorOf(Props.create(UploadManager.class, new ActorCreator<UploadManager>() {
            @Override
            public UploadManager create() {
                return new UploadManager(context());
            }
        }).changeDispatcher("heavy"), "actor/upload/manager");
    }

    public KeyValueEngine<Downloaded> getDownloadedEngine() {
        return downloadedEngine;
    }

    public void bindFile(FileReference fileReference, boolean isAutostart, FileCallback callback) {
        downloadManager.send(new DownloadManager.BindDownload(fileReference, isAutostart, callback));
    }

    public void unbindFile(long fileId, FileCallback callback, boolean cancel) {
        downloadManager.send(new DownloadManager.UnbindDownload(fileId, cancel, callback));
    }

    public void requestState(long fileId, final FileCallback callback) {
        downloadManager.send(new DownloadManager.RequestState(fileId, new FileCallback() {
            @Override
            public void onNotDownloaded() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onNotDownloaded();
                    }
                });
            }

            @Override
            public void onDownloading(final float progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDownloading(progress);
                    }
                });
            }

            @Override
            public void onDownloaded(final FileSystemReference reference) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDownloaded(reference);
                    }
                });
            }
        }));
    }

    public void startDownloading(FileReference location) {
        downloadManager.send(new DownloadManager.StartDownload(location));
    }

    public void cancelDownloading(long fileId) {
        downloadManager.send(new DownloadManager.CancelDownload(fileId));
    }

    // Upload

    public void bindUploadFile(long rid, UploadFileCallback uploadFileCallback) {
        uploadManager.send(new UploadManager.BindUpload(rid, uploadFileCallback));
    }

    public void unbindUploadFile(long rid, UploadFileCallback callback) {
        uploadManager.send(new UploadManager.UnbindUpload(rid, callback));
    }

    public void requestUpload(long rid, String descriptor, String fileName, ActorRef requester) {
        uploadManager.send(new UploadManager.StartUpload(rid, descriptor, fileName), requester);
    }

    public void cancelUpload(long rid) {
        uploadManager.send(new UploadManager.StopUpload(rid));
    }

    public void requestUploadState(long rid, UploadFileCallback callback) {
        uploadManager.send(new UploadManager.RequestState(rid, callback));
    }

    public void resumeUpload(long rid) {
        uploadManager.send(new UploadManager.ResumeUpload(rid));
    }

    public void pauseUpload(long rid) {
        uploadManager.send(new UploadManager.PauseUpload(rid));
    }

    public String getDownloadedDescriptor(long fileId) {
        Downloaded downloaded = downloadedEngine.getValue(fileId);
        if (downloaded == null) {
            return null;
        } else {
            return downloaded.getDescriptor();
        }
    }

    public void resetModule() {
        // TODO: Implement
    }
}