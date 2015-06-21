/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.file;

import im.actor.model.FileSystemProvider;
import im.actor.model.HttpProvider;
import im.actor.model.api.FileLocation;
import im.actor.model.api.rpc.RequestGetFileUrl;
import im.actor.model.api.rpc.ResponseGetFileUrl;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;
import im.actor.model.files.OutputFile;
import im.actor.model.http.FileDownloadCallback;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

public class DownloadTask extends ModuleActor {

    private static final int SIM_BLOCKS_COUNT = 4;

    private final String TAG;
    private final boolean LOG;

    private FileReference fileReference;
    private ActorRef manager;
    private FileSystemProvider fileSystemProvider;
    private HttpProvider downloaderProvider;

    private FileSystemReference destReference;
    private OutputFile outputFile;

    private boolean isCompleted;

    private String fileUrl;
    private int blockSize = 32 * 1024;
    private int blocksCount;
    private int nextBlock = 0;
    private int currentDownloads = 0;
    private int downloaded = 0;

    public DownloadTask(FileReference fileReference, ActorRef manager, Modules messenger) {
        super(messenger);
        this.TAG = "DownloadTask{" + fileReference.getFileId() + "}";
        this.LOG = messenger.getConfiguration().isEnableFilesLogging();
        this.fileReference = fileReference;
        this.manager = manager;
    }

    @Override
    public void preStart() {

        if (LOG) {
            Log.d(TAG, "Creating file...");
        }

        fileSystemProvider = modules().getConfiguration().getFileSystemProvider();
        if (fileSystemProvider == null) {
            reportError();
            if (LOG) {
                Log.d(TAG, "No FileSystem available");
            }
            return;
        }

        downloaderProvider = modules().getConfiguration().getHttpProvider();
        if (downloaderProvider == null) {
            reportError();
            if (LOG) {
                Log.d(TAG, "No HTTP Support available");
            }
            return;
        }

        destReference = fileSystemProvider.createTempFile();
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

    private void requestUrl() {
        if (LOG) {
            Log.d(TAG, "Loading url...");
        }
        request(new RequestGetFileUrl(new FileLocation(fileReference.getFileId(),
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

        FileSystemReference reference = fileSystemProvider.commitTempFile(destReference, fileReference);
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

            downloadPart(blockIndex, offset);

            checkQueue();
        } else {
            if (LOG) {
                Log.d(TAG, "Task queue is full");
            }
        }
    }

    private void downloadPart(final int blockIndex, final int fileOffset) {
        downloaderProvider.getMethod(fileUrl, fileOffset, blockSize, fileReference.getFileSize(), new FileDownloadCallback() {
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
            public void onDownloadFailure() {
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
        manager.send(new DownloadManager.OnDownloadProgress(fileReference.getFileId(), progress));
    }

    private void reportComplete(FileSystemReference reference) {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        manager.send(new DownloadManager.OnDownloaded(fileReference.getFileId(), reference));
    }
}
