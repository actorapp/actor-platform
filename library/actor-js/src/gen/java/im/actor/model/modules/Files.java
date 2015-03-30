package im.actor.model.modules;

import java.io.IOException;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;
import im.actor.model.viewmodel.DownloadCallback;
import im.actor.model.modules.file.DownloadManager;
import im.actor.model.modules.file.entity.Downloaded;
import im.actor.model.viewmodel.UploadCallback;
import im.actor.model.modules.file.UploadManager;
import im.actor.model.modules.utils.BaseKeyValueEngine;
import im.actor.model.droidkit.engine.KeyValueEngine;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class Files extends BaseModule {

    private KeyValueEngine<Downloaded> downloadedEngine;
    private ActorRef downloadManager;
    private ActorRef uploadManager;

    public Files(final Modules modules) {
        super(modules);

        downloadedEngine = new BaseKeyValueEngine<Downloaded>(
                modules.getConfiguration().getStorageProvider().createKeyValue(STORAGE_DOWNLOADS)) {
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
                return new DownloadManager(modules());
            }
        }), "actor/download/manager");
        uploadManager = system().actorOf(Props.create(UploadManager.class, new ActorCreator<UploadManager>() {
            @Override
            public UploadManager create() {
                return new UploadManager(modules());
            }
        }), "actor/upload/manager");
    }

    public KeyValueEngine<Downloaded> getDownloadedEngine() {
        return downloadedEngine;
    }

    public void bindFile(FileReference fileReference, boolean isAutostart, DownloadCallback callback) {
        downloadManager.send(new DownloadManager.BindDownload(fileReference, isAutostart, callback));
    }

    public void unbindFile(long fileId, DownloadCallback callback, boolean cancel) {
        downloadManager.send(new DownloadManager.UnbindDownload(fileId, cancel, callback));
    }

    public void requestState(long fileId, final DownloadCallback callback) {
        downloadManager.send(new DownloadManager.RequestState(fileId, new DownloadCallback() {
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

    public void bindUploadFile(long rid, UploadCallback uploadCallback) {
        uploadManager.send(new UploadManager.BindUpload(rid, uploadCallback));
    }

    public void unbindUploadFile(long rid, UploadCallback callback) {
        uploadManager.send(new UploadManager.UnbindUpload(rid, callback));
    }

    public void requestUpload(long rid, String descriptor, String fileName, ActorRef requester) {
        uploadManager.send(new UploadManager.StartUpload(rid, descriptor, fileName), requester);
    }

    public void cancelUpload(long rid) {
        uploadManager.send(new UploadManager.StopUpload(rid));
    }

    public void requestUploadState(long rid, UploadCallback callback) {
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
}