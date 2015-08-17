/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.file;

import java.util.ArrayList;

import im.actor.core.entity.FileReference;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.file.entity.Downloaded;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.modules.utils.RandomUtils;
import im.actor.core.viewmodel.UploadFileCallback;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.files.FileSystemReference;

public class UploadManager extends ModuleActor {

    private static final String TAG = "UploadManager";

    private static final int SIM_MAX_UPLOADS = 2;

    private final boolean LOG;

    private ArrayList<QueueItem> queue = new ArrayList<QueueItem>();

    public UploadManager(ModuleContext context) {
        super(context);
        this.LOG = context.getConfiguration().isEnableFilesLogging();
    }

    // Tasks

    public void startUpload(long rid, String descriptor, String fileName, ActorRef requestActor) {
        if (LOG) {
            Log.d(TAG, "Starting upload #" + rid + " with descriptor " + descriptor);
        }
        QueueItem queueItem = new QueueItem(rid, descriptor, fileName, requestActor);
        queueItem.isStopped = false;
        queue.add(queueItem);
        checkQueue();
    }

    public void stopUpload(long rid) {
        if (LOG) {
            Log.d(TAG, "Stopping download #" + rid);
        }
        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            if (LOG) {
                Log.d(TAG, "- Not present in queue");
            }
        } else {
            if (queueItem.isStarted) {
                if (LOG) {
                    Log.d(TAG, "- Stopping actor");
                }
                queueItem.taskRef.send(PoisonPill.INSTANCE);
                queueItem.taskRef = null;
                queueItem.isStarted = false;
            }
            queue.remove(queueItem);
            for (final UploadFileCallback callback : queueItem.callbacks) {
                im.actor.runtime.Runtime.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        callback.onNotUploading();
                    }
                });
            }
        }
        checkQueue();
    }

    public void bindUpload(long rid, final UploadFileCallback callback) {
        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            im.actor.runtime.Runtime.dispatch(new Runnable() {
                @Override
                public void run() {
                    callback.onNotUploading();
                }
            });
        } else {
            if (queueItem.isStopped) {
                im.actor.runtime.Runtime.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        callback.onNotUploading();
                    }
                });
            } else {
                final float progress = queueItem.progress;
                im.actor.runtime.Runtime.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        callback.onUploading(progress);
                    }
                });
            }
            queueItem.callbacks.add(callback);
        }
    }

    public void unbindUpload(long rid, UploadFileCallback callback) {
        QueueItem queueItem = findItem(rid);
        if (queueItem != null) {
            queueItem.callbacks.remove(callback);
        }
    }

    public void requestState(long rid, final UploadFileCallback callback) {
        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            im.actor.runtime.Runtime.dispatch(new Runnable() {
                @Override
                public void run() {
                    callback.onNotUploading();
                }
            });
        } else {
            if (queueItem.isStopped) {
                im.actor.runtime.Runtime.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        callback.onNotUploading();
                    }
                });
            } else {
                final float progress = queueItem.progress;
                im.actor.runtime.Runtime.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        callback.onUploading(progress);
                    }
                });
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
            for (final UploadFileCallback callback : queueItem.callbacks) {
                im.actor.runtime.Runtime.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        callback.onUploading(0);
                    }
                });
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
            for (final UploadFileCallback callback : queueItem.callbacks) {
                im.actor.runtime.Runtime.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        callback.onNotUploading();
                    }
                });
            }
        }
    }

    // Queue processing

    public void onUploadTaskError(long rid) {
        if (LOG) {
            Log.d(TAG, "Upload #" + rid + " error");
        }
        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            if (LOG) {
                Log.d(TAG, "- Nothing found");
            }
            return;
        }

        if (!queueItem.isStarted) {
            return;
        }

        queueItem.taskRef.send(PoisonPill.INSTANCE);
        queueItem.isStopped = true;
        queueItem.isStarted = false;

        for (final UploadFileCallback callback : queueItem.callbacks) {
            im.actor.runtime.Runtime.dispatch(new Runnable() {
                @Override
                public void run() {
                    callback.onNotUploading();
                }
            });
        }

        queueItem.requestActor.send(new UploadError(rid));

        checkQueue();
    }

    public void onUploadTaskProgress(long rid, final float progress) {
        if (LOG) {
            Log.d(TAG, "Upload #" + rid + " progress " + progress);
        }

        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            return;
        }

        if (!queueItem.isStarted) {
            return;
        }

        queueItem.progress = progress;

        for (final UploadFileCallback fileCallback : queueItem.callbacks) {
            im.actor.runtime.Runtime.dispatch(new Runnable() {
                @Override
                public void run() {
                    fileCallback.onUploading(progress);
                }
            });
        }
    }

    public void onUploadTaskComplete(long rid, FileReference fileReference, FileSystemReference reference) {
        if (LOG) {
            Log.d(TAG, "Upload #" + rid + " complete");
        }

        QueueItem queueItem = findItem(rid);
        if (queueItem == null) {
            return;
        }

        if (!queueItem.isStarted) {
            return;
        }

        queue.remove(queueItem);
        queueItem.taskRef.send(PoisonPill.INSTANCE);

        // Saving reference to uploaded file
        context().getFilesModule().getDownloadedEngine().addOrUpdateItem(new Downloaded(fileReference.getFileId(),
                fileReference.getFileSize(), reference.getDescriptor()));

        for (final UploadFileCallback fileCallback : queueItem.callbacks) {
            im.actor.runtime.Runtime.dispatch(new Runnable() {
                @Override
                public void run() {
                    fileCallback.onUploaded();
                }
            });
        }

        queueItem.requestActor.send(new UploadCompleted(rid, fileReference));

        checkQueue();
    }

    private void checkQueue() {
        if (LOG) {
            Log.d(TAG, "- Checking queue");
        }

        int activeUploads = 0;
        for (QueueItem queueItem : queue) {
            if (queueItem.isStarted) {
                activeUploads++;
            }
        }

        if (activeUploads >= SIM_MAX_UPLOADS) {
            if (LOG) {
                Log.d(TAG, "- Already have max number of simultaneous uploads");
            }
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
            if (LOG) {
                Log.d(TAG, "- No work for downloading");
            }
            return;
        }

        if (LOG) {
            Log.d(TAG, "- Starting upload file #" + pendingQueue.fileDescriptor);
        }

        pendingQueue.isStarted = true;

        final QueueItem finalPendingQueue = pendingQueue;
        pendingQueue.taskRef = system().actorOf(Props.create(UploadTask.class, new ActorCreator<UploadTask>() {
            @Override
            public UploadTask create() {
                return new UploadTask(finalPendingQueue.rid, finalPendingQueue.fileDescriptor,
                        finalPendingQueue.fileName, self(), context());
            }
        }).changeDispatcher("heavy"), "actor/upload/task_" + RandomUtils.nextRid());
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
        private String fileName;
        private ArrayList<UploadFileCallback> callbacks = new ArrayList<UploadFileCallback>();

        private QueueItem(long rid, String fileDescriptor, String fileName, ActorRef requestActor) {
            this.rid = rid;
            this.fileDescriptor = fileDescriptor;
            this.requestActor = requestActor;
            this.fileName = fileName;
        }
    }

    //region Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof StartUpload) {
            StartUpload startUpload = (StartUpload) message;
            startUpload(startUpload.getRid(), startUpload.getFileDescriptor(),
                    startUpload.getFileName(), sender());
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
            onUploadTaskComplete(taskComplete.getRid(), taskComplete.getLocation(),
                    taskComplete.getReference());
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
        private String fileName;

        public StartUpload(long rid, String fileDescriptor, String fileName) {
            this.rid = rid;
            this.fileDescriptor = fileDescriptor;
            this.fileName = fileName;
        }

        public long getRid() {
            return rid;
        }

        public String getFileDescriptor() {
            return fileDescriptor;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class BindUpload {
        private long rid;
        private UploadFileCallback callback;

        public BindUpload(long rid, UploadFileCallback callback) {
            this.rid = rid;
            this.callback = callback;
        }

        public long getRid() {
            return rid;
        }

        public UploadFileCallback getCallback() {
            return callback;
        }
    }

    public static class UnbindUpload {
        private long rid;
        private UploadFileCallback callback;

        public UnbindUpload(long rid, UploadFileCallback callback) {
            this.rid = rid;
            this.callback = callback;
        }

        public long getRid() {
            return rid;
        }

        public UploadFileCallback getCallback() {
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
        private FileReference location;
        private FileSystemReference reference;

        public UploadTaskComplete(long rid, FileReference location, FileSystemReference reference) {
            this.rid = rid;
            this.location = location;
            this.reference = reference;
        }

        public long getRid() {
            return rid;
        }

        public FileSystemReference getReference() {
            return reference;
        }

        public FileReference getLocation() {
            return location;
        }
    }

    public static class UploadCompleted {
        private long rid;
        private FileReference fileReference;

        public UploadCompleted(long rid, FileReference fileReference) {
            this.rid = rid;
            this.fileReference = fileReference;
        }

        public long getRid() {
            return rid;
        }

        public FileReference getFileReference() {
            return fileReference;
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
        private UploadFileCallback callback;

        public RequestState(long rid, UploadFileCallback callback) {
            this.rid = rid;
            this.callback = callback;
        }

        public long getRid() {
            return rid;
        }

        public UploadFileCallback getCallback() {
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
