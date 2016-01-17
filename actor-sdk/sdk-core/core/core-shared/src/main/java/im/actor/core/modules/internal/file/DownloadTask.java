/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.file;

import im.actor.core.api.ApiFileLocation;
import im.actor.core.api.rpc.RequestGetFileUrl;
import im.actor.core.api.rpc.ResponseGetFileUrl;
import im.actor.core.entity.FileReference;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.HTTP;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.files.OutputFile;
import im.actor.runtime.http.FileDownloadCallback;

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

    private String fileUrl;
    private int blockSize = 32 * 1024;
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

        outputFile = destReference.openWrite(fileReference.getFileSize());
        if (outputFile == null) {
            reportError();
            if (LOG) {
                Log.d(TAG, "Unable to write wile");
            }
            return;
        }

        requestUrl();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof NotifyProgress) {
            performReportProgress();
        } else if (message instanceof Retry) {
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
        request(new RequestGetFileUrl(new ApiFileLocation(fileReference.getFileId(),
                fileReference.getAccessHash())), new RpcCallback<ResponseGetFileUrl>() {
            @Override
            public void onResult(ResponseGetFileUrl response) {
                fileUrl = response.getUrl();
                if (LOG) {
                    Log.d(TAG, "Loaded file url: " + fileUrl);
                }
                startDownload();
            }

            @Override
            public void onError(RpcException e) {
                if (LOG) {
                    Log.d(TAG, "Unable to load file url");
                }
                reportError();
            }
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
        HTTP.getMethod(fileUrl, fileOffset, blockSize, fileReference.getFileSize(), new FileDownloadCallback() {
            @Override
            public void onDownloaded(final byte[] data) {
                self().send(new Runnable() {
                    @Override
                    public void run() {
                        downloaded++;
                        if (LOG) {
                            Log.d(TAG, "Download part #" + blockIndex + " completed");
                        }
                        if (!outputFile.write(fileOffset, data, 0, data.length)) {
                            reportError();
                            return;
                        }
                        currentDownloads--;
                        reportProgress(downloaded / (float) blocksCount);
                        checkQueue();
                    }
                });
            }

            @Override
            public void onDownloadFailure(int error, int retryInSecs) {
                if ((error >= 500 && error < 600) || error == 0) {
                    // Server on unknown error

                    if (retryInSecs <= 0) {
                        retryInSecs = DEFAULT_RETRY;
                    }

                    if (LOG) {
                        Log.w(TAG, "Download part #" + blockIndex + " failure #" + error + " trying again in " + retryInSecs + " sec, attempt #" + (attempt + 1));
                    }

                    self().send(new Retry(blockIndex, fileOffset, attempt + 1));
                } else {
                    self().send(new Runnable() {
                        @Override
                        public void run() {
                            if (LOG) {
                                Log.d(TAG, "Download part #" + blockIndex + " failure");
                            }
                            reportError();
                        }
                    });
                }
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
        if (delta > NOTIFY_THROTTLE) {
            lastNotifyDate = im.actor.runtime.Runtime.getActorTime();
            self().send(new NotifyProgress());
        } else {
            self().sendOnce(new NotifyProgress(), delta);
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

    private class NotifyProgress {

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
