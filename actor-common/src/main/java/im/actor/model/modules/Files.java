package im.actor.model.modules;

import java.io.IOException;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.FileLocation;
import im.actor.model.files.FileReference;
import im.actor.model.modules.file.DownloadManager;
import im.actor.model.modules.file.Downloaded;
import im.actor.model.modules.file.FileCallback;
import im.actor.model.modules.utils.BaseKeyValueEngine;
import im.actor.model.storage.KeyValueEngine;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class Files extends BaseModule {

    private KeyValueEngine<Downloaded> downloadedEngine;
    private ActorRef downloadManager;

    public Files(final Modules modules) {
        super(modules);

        downloadedEngine = new BaseKeyValueEngine<Downloaded>(
                modules.getConfiguration().getStorage().createDownloadsEngine()) {
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
    }

    public KeyValueEngine<Downloaded> getDownloadedEngine() {
        return downloadedEngine;
    }

    public void bindFile(FileLocation fileLocation, boolean isAutostart, FileCallback callback) {
        downloadManager.send(new DownloadManager.BindDownload(fileLocation, isAutostart, callback));
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
            public void onDownloaded(final FileReference reference) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDownloaded(reference);
                    }
                });
            }
        }));
    }

    public void cancelDownloading(long fileId) {
        downloadManager.send(new DownloadManager.CancelDownload(fileId));
    }

    public void startDownloading(FileLocation location) {
        downloadManager.send(new DownloadManager.StartDownload(location));
    }
}