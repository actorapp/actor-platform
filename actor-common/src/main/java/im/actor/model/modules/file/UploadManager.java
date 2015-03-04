package im.actor.model.modules.file;

import java.util.ArrayList;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.actors.messages.PoisonPill;
import im.actor.model.entity.FileLocation;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.modules.utils.RandomUtils;

/**
 * Created by ex3ndr on 03.03.15.
 */
public class UploadManager extends ModuleActor {

    private static final String TAG = "UploadManager";

    private static final int SIM_MAX_UPLOADS = 2;

    private ArrayList<QueueItem> queue = new ArrayList<QueueItem>();

    public UploadManager(Modules messenger) {
        super(messenger);
    }

    // Tasks

    public void startUpload(long rid, String descriptor, ActorRef requestActor) {
        Log.d(TAG, "Starting upload #" + rid + " with descriptor " + descriptor);
        QueueItem queueItem = new QueueItem(rid, descriptor, requestActor);
        queueItem.isStopped = false;
        queue.add(queueItem);
        checkQueue();
    }

    public void stopUpload(long rid) {
        Log.d(TAG, "Stopping download #" + rid);
        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            Log.d(TAG, "- Not present in queue");
        } else {
            if (queueItem.isStarted) {
                Log.d(TAG, "- Stopping actor");
                queueItem.taskRef.send(PoisonPill.INSTANCE);
                queueItem.taskRef = null;
                queueItem.isStarted = false;
            }
            queue.remove(queueItem);
            for (UploadCallback callback : queueItem.callbacks) {
                callback.onNotUploading();
            }
        }
        checkQueue();
    }

    public void bindUpload(long rid, UploadCallback callback) {
        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            callback.onNotUploading();
        } else {
            if (queueItem.isStopped) {
                callback.onNotUploading();
            } else {
                callback.onUploading(queueItem.progress);
            }
            queueItem.callbacks.add(callback);
        }
    }

    public void unbindUpload(long rid, UploadCallback callback) {
        QueueItem queueItem = findItem(rid);
        if (queueItem != null) {
            queueItem.callbacks.remove(callback);
        }
    }

    public void requestState(long rid, UploadCallback callback) {
        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            callback.onNotUploading();
        } else {
            if (queueItem.isStopped) {
                callback.onNotUploading();
            } else {
                callback.onUploading(queueItem.progress);
            }
        }
    }

    public void resumeUpload(long rid) {
        QueueItem queueItem = findItem(rid);
        if (queueItem != null) {
            if (queueItem.isStarted) {
                return;
            }
            if (queueItem.isStopped) {
                queueItem.isStopped = false;
            }
            queueItem.progress = 0;
            for (UploadCallback callback : queueItem.callbacks) {
                callback.onUploading(0);
            }
            checkQueue();
        }
    }

    public void pauseUpload(long rid) {
        QueueItem queueItem = findItem(rid);
        if (queueItem != null) {
            if (queueItem.isStarted) {
                queueItem.taskRef.send(PoisonPill.INSTANCE);
                queueItem.taskRef = null;
                queueItem.isStarted = false;
            }
            queueItem.isStopped = true;
            for (UploadCallback callback : queueItem.callbacks) {
                callback.onNotUploading();
            }
        }
    }

    // Queue processing

    public void onUploadTaskError(long rid) {
        Log.d(TAG, "Upload #" + rid + " error");
        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            Log.d(TAG, "- Nothing found");
            return;
        }

        if (!queueItem.isStarted) {
            return;
        }

        queueItem.taskRef.send(PoisonPill.INSTANCE);
        queueItem.isStopped = true;
        queueItem.isStarted = false;

        for (UploadCallback callback : queueItem.callbacks) {
            callback.onNotUploading();
        }

        queueItem.requestActor.send(new UploadError(rid));
    }

    public void onUploadTaskProgress(long rid, float progress) {
        Log.d(TAG, "Upload #" + rid + " progress " + progress);

        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            return;
        }

        if (!queueItem.isStarted) {
            return;
        }

        queueItem.progress = progress;

        for (UploadCallback fileCallback : queueItem.callbacks) {
            fileCallback.onUploading(progress);
        }
    }

    public void onUploadTaskComplete(long rid, FileLocation fileLocation) {
        Log.d(TAG, "Upload #" + rid + " complete");

        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            return;
        }

        if (!queueItem.isStarted) {
            return;
        }

        queue.remove(queueItem);
        queueItem.taskRef.send(PoisonPill.INSTANCE);

        for (UploadCallback fileCallback : queueItem.callbacks) {
            fileCallback.onUploaded();
        }

        queueItem.requestActor.send(new UploadCompleted(rid, fileLocation));
    }

    private void checkQueue() {
        Log.d(TAG, "- Checking queue");

        int activeUploads = 0;
        for (QueueItem queueItem : queue) {
            if (queueItem.isStarted) {
                activeUploads++;
            }
        }

        if (activeUploads >= SIM_MAX_UPLOADS) {
            Log.d(TAG, "- Already have max number of simultaneous uploads");
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

        Log.d(TAG, "- Starting upload file #" + pendingQueue.fileDescriptor);

        pendingQueue.isStarted = true;

        final QueueItem finalPendingQueue = pendingQueue;
        pendingQueue.taskRef = system().actorOf(Props.create(UploadTask.class, new ActorCreator<UploadTask>() {
            @Override
            public UploadTask create() {
                return new UploadTask(finalPendingQueue.rid, finalPendingQueue.fileDescriptor, self(), modules());
            }
        }), "actor/upload/task_" + RandomUtils.nextRid());
    }

    private QueueItem findItem(long rid) {
        for (QueueItem q : queue) {
            if (q.rid == rid) {
                return q;
            }
        }
        return null;
    }

    private class QueueItem {
        private long rid;
        private String fileDescriptor;
        private boolean isStopped;
        private boolean isStarted;
        private float progress;
        private ActorRef taskRef;
        private ActorRef requestActor;
        private ArrayList<UploadCallback> callbacks = new ArrayList<UploadCallback>();

        private QueueItem(long rid, String fileDescriptor, ActorRef requestActor) {
            this.rid = rid;
            this.fileDescriptor = fileDescriptor;
            this.requestActor = requestActor;
        }
    }

    //region Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof StartUpload) {
            StartUpload startUpload = (StartUpload) message;
            startUpload(startUpload.getRid(), startUpload.getFileDescriptor(), sender());
        } else if (message instanceof StopUpload) {
            StopUpload cancelUpload = (StopUpload) message;
            stopUpload(cancelUpload.getRid());
        } else if (message instanceof UploadTaskError) {
            UploadTaskError uploadTaskError = (UploadTaskError) message;
            onUploadTaskError(uploadTaskError.getRid());
        } else if (message instanceof UploadTaskProgress) {
            UploadTaskProgress taskProgress = (UploadTaskProgress) message;
            onUploadTaskProgress(taskProgress.getRid(), taskProgress.getProgress());
        } else if (message instanceof UploadTaskComplete) {
            UploadTaskComplete taskComplete = (UploadTaskComplete) message;
            onUploadTaskComplete(taskComplete.getRid(), taskComplete.getLocation());
        } else if (message instanceof BindUpload) {
            BindUpload bindUpload = (BindUpload) message;
            bindUpload(bindUpload.getRid(), bindUpload.getCallback());
        } else if (message instanceof UnbindUpload) {
            UnbindUpload unbindUpload = (UnbindUpload) message;
            unbindUpload(unbindUpload.getRid(), unbindUpload.getCallback());
        } else if (message instanceof RequestState) {
            RequestState requestState = (RequestState) message;
            requestState(requestState.getRid(), requestState.getCallback());
        } else if (message instanceof PauseUpload) {
            PauseUpload pauseUpload = (PauseUpload) message;
            pauseUpload(pauseUpload.getRid());
        } else if (message instanceof ResumeUpload) {
            ResumeUpload resumeUpload = (ResumeUpload) message;
            resumeUpload(resumeUpload.getRid());
        } else {
            drop(message);
        }
    }

    public static class StartUpload {
        private long rid;
        private String fileDescriptor;

        public StartUpload(long rid, String fileDescriptor) {
            this.rid = rid;
            this.fileDescriptor = fileDescriptor;
        }

        public long getRid() {
            return rid;
        }

        public String getFileDescriptor() {
            return fileDescriptor;
        }
    }

    public static class BindUpload {
        private long rid;
        private UploadCallback callback;

        public BindUpload(long rid, UploadCallback callback) {
            this.rid = rid;
            this.callback = callback;
        }

        public long getRid() {
            return rid;
        }

        public UploadCallback getCallback() {
            return callback;
        }
    }

    public static class UnbindUpload {
        private long rid;
        private UploadCallback callback;

        public UnbindUpload(long rid, UploadCallback callback) {
            this.rid = rid;
            this.callback = callback;
        }

        public long getRid() {
            return rid;
        }

        public UploadCallback getCallback() {
            return callback;
        }
    }

    public static class StopUpload {
        private long rid;

        public StopUpload(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class UploadTaskError {
        private long rid;

        public UploadTaskError(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class UploadTaskProgress {
        private long rid;
        private float progress;

        public UploadTaskProgress(long rid, float progress) {
            this.rid = rid;
            this.progress = progress;
        }

        public long getRid() {
            return rid;
        }

        public float getProgress() {
            return progress;
        }
    }

    public static class UploadTaskComplete {
        private long rid;
        private FileLocation location;

        public UploadTaskComplete(long rid, FileLocation location) {
            this.rid = rid;
            this.location = location;
        }

        public long getRid() {
            return rid;
        }

        public FileLocation getLocation() {
            return location;
        }
    }

    public static class UploadCompleted {
        private long rid;
        private FileLocation fileLocation;

        public UploadCompleted(long rid, FileLocation fileLocation) {
            this.rid = rid;
            this.fileLocation = fileLocation;
        }

        public long getRid() {
            return rid;
        }

        public FileLocation getFileLocation() {
            return fileLocation;
        }
    }

    public static class UploadError {
        private long rid;

        public UploadError(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class RequestState {
        private long rid;
        private UploadCallback callback;

        public RequestState(long rid, UploadCallback callback) {
            this.rid = rid;
            this.callback = callback;
        }

        public long getRid() {
            return rid;
        }

        public UploadCallback getCallback() {
            return callback;
        }
    }

    public static class PauseUpload {
        private long rid;

        public PauseUpload(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class ResumeUpload {
        private long rid;

        public ResumeUpload(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    //endregion
}
