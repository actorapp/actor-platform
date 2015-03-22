package im.actor.model.modules.file;

import java.util.ArrayList;

import im.actor.model.FileSystemProvider;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.actors.messages.PoisonPill;
import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.modules.utils.RandomUtils;
import im.actor.model.droidkit.engine.KeyValueEngine;
import im.actor.model.viewmodel.DownloadCallback;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class DownloadManager extends ModuleActor {

    private static final String TAG = "DownloadManager";
    private static final int SIM_MAX_DOWNLOADS = 2;

    private ArrayList<QueueItem> queue = new ArrayList<QueueItem>();

    private KeyValueEngine<Downloaded> downloaded;

    public DownloadManager(Modules messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        super.preStart();
        downloaded = modules().getFilesModule().getDownloadedEngine();
    }

    // Tasks

    public void requestState(long fileId, DownloadCallback callback) {
        Log.d(TAG, "Requesting state file #" + fileId);

        Downloaded downloaded1 = downloaded.getValue(fileId);
        if (downloaded1 != null) {
            FileSystemProvider provider = modules().getConfiguration().getFileSystemProvider();
            FileSystemReference reference = provider.fileFromDescriptor(downloaded1.getDescriptor());
            boolean isExist = reference.isExist();
            int fileSize = reference.getSize();
            if (isExist && fileSize == downloaded1.getFileSize()) {
                Log.d(TAG, "- Downloaded");
                callback.onDownloaded(modules().getConfiguration().getFileSystemProvider()
                        .fileFromDescriptor(downloaded1.getDescriptor()));
                return;
            } else {
                Log.d(TAG, "- File is corrupted");
                if (!isExist) {
                    Log.d(TAG, "- File not found");
                }
                if (fileSize != downloaded1.getFileSize()) {
                    Log.d(TAG, "- Incorrect file size. Expected: " + downloaded1.getFileSize() + ", got: " + fileSize);
                }
                downloaded.removeItem(downloaded1.getFileId());
            }
        }

        QueueItem queueItem = findItem(fileId);
        if (queueItem == null) {
            callback.onNotDownloaded();
        } else {
            if (queueItem.isStarted) {
                callback.onDownloading(queueItem.progress);
            } else if (queueItem.isStopped) {
                callback.onNotDownloaded();
            } else {
                callback.onDownloading(0);
            }
        }
    }

    public void bindDownload(final FileReference fileReference, boolean autoStart, DownloadCallback callback) {
        Log.d(TAG, "Binding file #" + fileReference.getFileId());
        Downloaded downloaded1 = downloaded.getValue(fileReference.getFileId());
        if (downloaded1 != null) {
            FileSystemProvider provider = modules().getConfiguration().getFileSystemProvider();
            FileSystemReference reference = provider.fileFromDescriptor(downloaded1.getDescriptor());
            boolean isExist = reference.isExist();
            int fileSize = reference.getSize();
            if (isExist && fileSize == downloaded1.getFileSize()) {
                Log.d(TAG, "- Downloaded");
                callback.onDownloaded(modules().getConfiguration().getFileSystemProvider()
                        .fileFromDescriptor(downloaded1.getDescriptor()));
                return;
            } else {
                Log.d(TAG, "- File is corrupted");
                if (!isExist) {
                    Log.d(TAG, "- File not found");
                }
                if (fileSize != downloaded1.getFileSize()) {
                    Log.d(TAG, "- Incorrect file size. Expected: " + downloaded1.getFileSize() + ", got: " + fileSize);
                }
                downloaded.removeItem(downloaded1.getFileId());
            }
        }

        QueueItem queueItem = findItem(fileReference.getFileId());
        if (queueItem == null) {
            Log.d(TAG, "- Adding to queue");

            queueItem = new QueueItem(fileReference);
            queueItem.callbacks.add(callback);

            if (autoStart) {
                queueItem.isStopped = false;
                callback.onDownloading(0);
            } else {
                queueItem.isStopped = true;
                callback.onNotDownloaded();
            }

            queue.add(0, queueItem);
        } else {
            Log.d(TAG, "- Promoting in queue");

            promote(fileReference.getFileId());

            if (!queueItem.callbacks.contains(callback)) {
                queueItem.callbacks.add(callback);
            }

            if (queueItem.isStopped) {
                callback.onNotDownloaded();
            } else {
                if (queueItem.isStarted) {
                    callback.onDownloading(queueItem.progress);
                } else {
                    callback.onDownloading(0);
                }
            }
        }

        checkQueue();
    }

    public void startDownload(FileReference fileReference) {
        Log.d(TAG, "Starting download #" + fileReference.getFileId());
        QueueItem queueItem = findItem(fileReference.getFileId());
        if (queueItem == null) {
            Log.d(TAG, "- Adding to queue");
            queueItem = new QueueItem(fileReference);
            queueItem.isStopped = false;
            queue.add(0, queueItem);
        } else {
            Log.d(TAG, "- Promoting in queue");
            if (queueItem.isStopped) {
                queueItem.isStopped = false;
                for (DownloadCallback callback : queueItem.callbacks) {
                    callback.onDownloading(0);
                }
            }
            promote(fileReference.getFileId());
        }

        checkQueue();
    }

    public void cancelDownload(long fileId) {
        Log.d(TAG, "Stopping download #" + fileId);
        QueueItem queueItem = findItem(fileId);
        if (queueItem == null) {
            Log.d(TAG, "- Not present in queue");
        } else {
            if (queueItem.isStarted) {
                Log.d(TAG, "- Stopping actor");
                queueItem.taskRef.send(PoisonPill.INSTANCE);
                queueItem.taskRef = null;
                queueItem.isStarted = false;
            }
            Log.d(TAG, "- Marking as stopped");
            queueItem.isStopped = true;

            for (DownloadCallback callback : queueItem.callbacks) {
                callback.onNotDownloaded();
            }
        }

        checkQueue();
    }

    public void unbindDownload(long fileId, boolean autoCancel, DownloadCallback callback) {
        Log.d(TAG, "Unbind file #" + fileId);
        QueueItem queueItem = findItem(fileId);
        if (queueItem == null) {
            Log.d(TAG, "- Not present in queue");
        } else {
            if (autoCancel) {
                if (queueItem.isStarted) {
                    Log.d(TAG, "- Stopping actor");
                    queueItem.taskRef.send(PoisonPill.INSTANCE);
                    queueItem.taskRef = null;
                    queueItem.isStarted = false;
                }

                if (!queueItem.isStopped) {
                    Log.d(TAG, "- Marking as stopped");
                    queueItem.isStopped = true;

                    for (DownloadCallback c : queueItem.callbacks) {
                        if (c != callback) {
                            c.onNotDownloaded();
                        }
                    }
                }
                queue.remove(queueItem);
            } else {
                Log.d(TAG, "- Removing callback");
                queueItem.callbacks.remove(callback);
            }
        }

        checkQueue();
    }

    // Queue processing

    private void checkQueue() {
        Log.d(TAG, "- Checking queue");

        int activeDownloads = 0;
        for (QueueItem queueItem : queue) {
            if (queueItem.isStarted) {
                activeDownloads++;
            }
        }

        if (activeDownloads >= SIM_MAX_DOWNLOADS) {
            Log.d(TAG, "- Already have max number of simultaneous downloads");
            return;
        }

        QueueItem pendingQueue = null;
        for (QueueItem queueItem : queue) {
            if (!queueItem.isStarted && !queueItem.isStopped) {
                pendingQueue = queueItem;
                break;
            }
        }
        if (pendingQueue == null) {
            Log.d(TAG, "- No work for downloading");
            return;
        }

        Log.d(TAG, "- Starting download file #" + pendingQueue.fileReference.getFileId());

        pendingQueue.isStarted = true;

        final QueueItem finalPendingQueue = pendingQueue;
        pendingQueue.taskRef = system().actorOf(Props.create(DownloadTask.class, new ActorCreator<DownloadTask>() {
            @Override
            public DownloadTask create() {
                return new DownloadTask(finalPendingQueue.fileReference, self(), modules());
            }
        }), "actor/download/task_" + RandomUtils.nextRid());
    }

    public void onDownloadProgress(long fileId, float progress) {
        Log.d(TAG, "onDownloadProgress file #" + fileId + " " + progress);
        QueueItem queueItem = findItem(fileId);
        if (queueItem == null) {
            return;
        }

        if (!queueItem.isStarted) {
            return;
        }

        queueItem.progress = progress;

        for (DownloadCallback fileCallback : queueItem.callbacks) {
            fileCallback.onDownloading(progress);
        }
    }

    public void onDownloaded(long fileId, FileSystemReference reference) {
        Log.d(TAG, "onDownloaded file #" + fileId);
        QueueItem queueItem = findItem(fileId);
        if (queueItem == null) {
            return;
        }

        if (!queueItem.isStarted) {
            return;
        }

        downloaded.addOrUpdateItem(new Downloaded(queueItem.fileReference.getFileId(),
                queueItem.fileReference.getFileSize(), reference.getDescriptor()));

        queue.remove(queueItem);
        queueItem.taskRef.send(PoisonPill.INSTANCE);

        for (DownloadCallback fileCallback : queueItem.callbacks) {
            fileCallback.onDownloaded(reference);
        }

        checkQueue();
    }

    public void onDownloadError(long fileId) {
        Log.d(TAG, "onDownloadError file #" + fileId);
        QueueItem queueItem = findItem(fileId);
        if (queueItem == null) {
            return;
        }

        if (!queueItem.isStarted) {
            return;
        }

        queueItem.taskRef.send(PoisonPill.INSTANCE);
        queueItem.isStopped = true;
        queueItem.isStarted = false;

        for (DownloadCallback fileCallback : queueItem.callbacks) {
            fileCallback.onNotDownloaded();
        }

        checkQueue();
    }

    private QueueItem findItem(long id) {
        for (QueueItem q : queue) {
            if (q.fileReference.getFileId() == id) {
                return q;
            }
        }
        return null;
    }

    private void promote(long id) {
        for (QueueItem q : queue) {
            if (q.fileReference.getFileId() == id) {
                if (!q.isStarted) {
                    queue.remove(q);
                    queue.add(0, q);
                }
                return;
            }
        }
    }

    private class QueueItem {
        private FileReference fileReference;
        private boolean isStopped;

        private ArrayList<DownloadCallback> callbacks = new ArrayList<DownloadCallback>();

        private boolean isStarted;
        private float progress;

        private ActorRef taskRef;

        private QueueItem(FileReference fileReference) {
            this.fileReference = fileReference;
        }
    }

    //region Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof BindDownload) {
            BindDownload requestDownload = (BindDownload) message;
            bindDownload(requestDownload.getFileReference(),
                    requestDownload.isAutostart(),
                    requestDownload.getCallback());
        } else if (message instanceof CancelDownload) {
            CancelDownload cancelDownload = (CancelDownload) message;
            cancelDownload(cancelDownload.getFileId());
        } else if (message instanceof UnbindDownload) {
            UnbindDownload unbindDownload = (UnbindDownload) message;
            unbindDownload(unbindDownload.getFileId(), unbindDownload.isAutocancel(), unbindDownload.getCallback());
        } else if (message instanceof StartDownload) {
            StartDownload startDownload = (StartDownload) message;
            startDownload(startDownload.getFileReference());
        } else if (message instanceof OnDownloadProgress) {
            OnDownloadProgress downloadProgress = (OnDownloadProgress) message;
            onDownloadProgress(downloadProgress.getFileId(), downloadProgress.getProgress());
        } else if (message instanceof OnDownloaded) {
            OnDownloaded onDownloaded = (OnDownloaded) message;
            onDownloaded(onDownloaded.getFileId(), onDownloaded.getReference());
        } else if (message instanceof OnDownloadedError) {
            OnDownloadedError error = (OnDownloadedError) message;
            onDownloadError(error.getFileId());
        } else if (message instanceof RequestState) {
            RequestState requestState = (RequestState) message;
            requestState(requestState.getFileId(), requestState.getCallback());
        } else {
            drop(message);
        }
    }

    public static class RequestState {
        private long fileId;
        private DownloadCallback callback;

        public RequestState(long fileId, DownloadCallback callback) {
            this.fileId = fileId;
            this.callback = callback;
        }

        public long getFileId() {
            return fileId;
        }

        public DownloadCallback getCallback() {
            return callback;
        }
    }

    public static class BindDownload {
        private FileReference fileReference;
        private boolean isAutostart;
        private DownloadCallback callback;

        public BindDownload(FileReference fileReference, boolean isAutostart, DownloadCallback callback) {
            this.fileReference = fileReference;
            this.isAutostart = isAutostart;
            this.callback = callback;
        }

        public FileReference getFileReference() {
            return fileReference;
        }

        public boolean isAutostart() {
            return isAutostart;
        }

        public DownloadCallback getCallback() {
            return callback;
        }
    }

    public static class StartDownload {
        private FileReference fileReference;

        public StartDownload(FileReference fileReference) {
            this.fileReference = fileReference;
        }

        public FileReference getFileReference() {
            return fileReference;
        }
    }

    public static class CancelDownload {
        private long fileId;

        public CancelDownload(long fileId) {
            this.fileId = fileId;
        }

        public long getFileId() {
            return fileId;
        }
    }

    public static class UnbindDownload {
        private long fileId;
        private boolean isAutocancel;
        private DownloadCallback callback;

        public UnbindDownload(long fileId, boolean isAutocancel, DownloadCallback callback) {
            this.fileId = fileId;
            this.isAutocancel = isAutocancel;
            this.callback = callback;
        }

        public long getFileId() {
            return fileId;
        }

        public DownloadCallback getCallback() {
            return callback;
        }

        public boolean isAutocancel() {
            return isAutocancel;
        }
    }

    public static class OnDownloadProgress {
        private long fileId;
        private float progress;

        public OnDownloadProgress(long fileId, float progress) {
            this.fileId = fileId;
            this.progress = progress;
        }

        public long getFileId() {
            return fileId;
        }

        public float getProgress() {
            return progress;
        }
    }

    public static class OnDownloaded {
        private long fileId;
        private FileSystemReference reference;

        public OnDownloaded(long fileId, FileSystemReference reference) {
            this.fileId = fileId;
            this.reference = reference;
        }

        public long getFileId() {
            return fileId;
        }

        public FileSystemReference getReference() {
            return reference;
        }
    }

    public static class OnDownloadedError {
        private long fileId;

        public OnDownloadedError(long fileId) {
            this.fileId = fileId;
        }

        public long getFileId() {
            return fileId;
        }
    }

    //endregion
}
