/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.file;

import im.actor.core.entity.FileReference;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.HTTP;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.files.OutputFile;
import im.actor.runtime.http.HTTPError;

public class DownloadTask extends ModuleActor {

    private static final int SIM_BLOCKS_COUNT = 4;
    private static final int NOTIFY_THROTTLE = 1000;
    private static final int DEFAULT_RETRY = 15;

    private final String TAG;
    private final boolean LOG;

    private FileReference fileReference;
    private ActorRef manager;

    private FileSystemReference destReference;
    private OutputFile outputFile;

    private boolean isCompleted;

    private long lastNotifyDate;
    private float currentProgress;
    private Cancellable notifyCancellable;

    private String fileUrl;
    private int blockSize = 128 * 1024;
    private int blocksCount;
    private int nextBlock = 0;
    private int currentDownloads = 0;
    private int downloaded = 0;

    public DownloadTask(FileReference fileReference, ActorRef manager, ModuleContext context) {
        super(context);
        this.TAG = "DownloadTask{" + fileReference.getFileId() + "}";
        this.LOG = context.getConfiguration().isEnableFilesLogging();
        this.fileReference = fileReference;
        this.manager = manager;
    }

    @Override
    public void preStart() {
        if (LOG) {
            Log.d(TAG, "Creating file...");
        }

        destReference = Storage.createTempFile();
        if (destReference == null) {
            reportError();
            if (LOG) {
                Log.d(TAG, "Unable to create reference");
            }
            return;
        }

        destReference.openWrite(fileReference.getFileSize()).then(r -> {
            outputFile = r;
            requestUrl();
        }).failure(e -> {
            reportError();
            if (LOG) {
                Log.d(TAG, "Unable to write wile");
            }
        });
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Retry) {
            Retry retry = (Retry) message;
            retryPart(retry.getBlockIndex(), retry.getFileOffset(), retry.getAttempt());
        } else {
            super.onReceive(message);
        }
    }

    private void requestUrl() {
        if (LOG) {
            Log.d(TAG, "Loading url...");
        }

        context().getFilesModule().getFileUrlInt().askForUrl(fileReference.getFileId(), fileReference.getAccessHash()).then(url -> {
            fileUrl = url;
            if (LOG) {
                Log.d(TAG, "Loaded file url: " + fileUrl);
            }
            startDownload();
        }).failure(e -> {
            if (LOG) {
                Log.d(TAG, "Unable to load file url");
            }
            reportError();
        });
    }

    private void startDownload() {
        blocksCount = fileReference.getFileSize() / blockSize;
        if (fileReference.getFileSize() % blockSize != 0) {
            blocksCount++;
        }

        if (LOG) {
            Log.d(TAG, "Starting downloading " + blocksCount + " blocks");
        }
        checkQueue();
    }

    private void completeDownload() {
        if (isCompleted) {
            return;
        }

        if (LOG) {
            Log.d(TAG, "Closing file...");
        }
        if (!outputFile.close()) {
            reportError();
            return;
        }

        FileSystemReference reference = Storage.commitTempFile(destReference, fileReference.getFileId(),
                fileReference.getFileName());
        if (reference == null) {
            reportError();
            return;
        }

        if (LOG) {
            Log.d(TAG, "Complete download {" + reference.getDescriptor() + "}");
        }
        reportComplete(reference);
    }

    private void checkQueue() {
        if (isCompleted) {
            return;
        }

        if (LOG) {
            Log.d(TAG, "checkQueue " + currentDownloads + "/" + nextBlock);
        }
        if (currentDownloads == 0 && nextBlock >= blocksCount) {
            completeDownload();
        } else if (currentDownloads < SIM_BLOCKS_COUNT && nextBlock < blocksCount) {
            currentDownloads++;
            int blockIndex = nextBlock++;
            int offset = blockIndex * blockSize;

            if (LOG) {
                Log.d(TAG, "Starting part #" + blockIndex + " download");
            }

            downloadPart(blockIndex, offset, 0);

            checkQueue();
        } else {
            if (LOG) {
                Log.d(TAG, "Task queue is full");
            }
        }
    }

    private void retryPart(int blockIndex, int fileOffset, int attempt) {
        if (isCompleted) {
            return;
        }

        if (LOG) {
            Log.d(TAG, "Trying again part #" + blockIndex + " download");
        }

        downloadPart(blockIndex, fileOffset, attempt);
    }

    private void downloadPart(final int blockIndex, final int fileOffset, final int attempt) {
        HTTP.getMethod(fileUrl, fileOffset, blockSize, fileReference.getFileSize()).then(r -> {
            downloaded++;
            if (LOG) {
                Log.d(TAG, "Download part #" + blockIndex + " completed");
            }
            if (!outputFile.write(fileOffset, r.getContent(), 0, r.getContent().length)) {
                reportError();
                return;
            }
            currentDownloads--;
            reportProgress(downloaded / (float) blocksCount);
            checkQueue();
        }).failure(e -> {
            if ((e instanceof HTTPError)
                    && ((((HTTPError) e).getErrorCode() >= 500
                    && ((HTTPError) e).getErrorCode() < 600)
                    || ((HTTPError) e).getErrorCode() == 0)) {
                // Server on unknown error
                int retryInSecs = DEFAULT_RETRY;

                if (LOG) {
                    Log.w(TAG, "Download part #" + blockIndex + " failure #" + ((HTTPError) e).getErrorCode() + " trying again in " + retryInSecs + " sec, attempt #" + (attempt + 1));
                }

                self().send(new Retry(blockIndex, fileOffset, attempt + 1));
            } else {
                if (LOG) {
                    Log.d(TAG, "Download part #" + blockIndex + " failure");
                }
                reportError();
            }
        });
    }

    private void reportError() {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        manager.send(new DownloadManager.OnDownloadedError(fileReference.getFileId()));
    }

    private void reportProgress(float progress) {
        if (isCompleted) {
            return;
        }

        if (progress > currentProgress) {
            currentProgress = progress;
        }

        long delta = im.actor.runtime.Runtime.getActorTime() - lastNotifyDate;

        if (notifyCancellable != null) {
            notifyCancellable.cancel();
            notifyCancellable = null;
        }

        if (delta > NOTIFY_THROTTLE) {
            lastNotifyDate = im.actor.runtime.Runtime.getActorTime();
            performReportProgress();
        } else {
            notifyCancellable = schedule((Runnable) () -> performReportProgress(), delta);
        }
    }

    private void performReportProgress() {
        if (isCompleted) {
            return;
        }
        manager.send(new DownloadManager.OnDownloadProgress(fileReference.getFileId(), currentProgress));
    }

    private void reportComplete(FileSystemReference reference) {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        manager.send(new DownloadManager.OnDownloaded(fileReference.getFileId(), reference));
    }

    private class Retry {

        private int blockIndex;
        private int fileOffset;
        private int attempt;

        public Retry(int blockIndex, int fileOffset, int attempt) {
            this.blockIndex = blockIndex;
            this.fileOffset = fileOffset;
            this.attempt = attempt;
        }

        public int getBlockIndex() {
            return blockIndex;
        }

        public int getFileOffset() {
            return fileOffset;
        }

        public int getAttempt() {
            return attempt;
        }
    }
}
