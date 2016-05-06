/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.file;

import im.actor.core.api.rpc.RequestCommitFileUpload;
import im.actor.core.api.rpc.RequestGetFileUploadPartUrl;
import im.actor.core.api.rpc.RequestGetFileUploadUrl;
import im.actor.core.api.rpc.ResponseCommitFileUpload;
import im.actor.core.api.rpc.ResponseGetFileUploadUrl;
import im.actor.core.entity.FileReference;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.HTTP;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.crypto.CRC32;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.files.InputFile;
import im.actor.runtime.files.OutputFile;
import im.actor.runtime.http.HTTPError;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.promise.Promise;

public class UploadTask extends ModuleActor {

    // j2objc workaround
    private static final HTTPResponse DUMB = null;

    private static final int SIM_BLOCKS_COUNT = 4;
    private static final int NOTIFY_THROTTLE = 1000;
    private static final int DEFAULT_RETRY = 15;

    private final String TAG;
    private final boolean LOG;

    private long rid;
    private String fileName;
    private String descriptor;

    private boolean isWriteToDestProvider = false;

    private FileSystemReference srcReference;
    private InputFile inputFile;

    private FileSystemReference destReference;
    private OutputFile outputFile;

    private ActorRef manager;
    private boolean isCompleted = false;

    private int blockSize = 128 * 1024;
    private int blocksCount;
    private int nextBlock = 0;
    private int uploaded;
    private int uploadCount;
    private long lastNotifyDate;
    private Cancellable notifyCancellable;

    private byte[] uploadConfig;
    private CRC32 crc32;

    private float currentProgress;
    private boolean alreadyInTemp;

    public UploadTask(long rid, String descriptor, String fileName, ActorRef manager, ModuleContext context) {
        super(context);
        this.LOG = context.getConfiguration().isEnableFilesLogging();
        this.rid = rid;
        this.fileName = fileName;
        this.descriptor = descriptor;
        this.manager = manager;
        this.TAG = "UploadTask{" + rid + "}";
    }

    @Override
    public void preStart() {
        alreadyInTemp = false;//Storage.isAlreadyInTemp(descriptor);
        isWriteToDestProvider = Storage.isFsPersistent() && !alreadyInTemp;

        srcReference = Storage.fileFromDescriptor(descriptor);
        if (srcReference == null) {
            if (LOG) {
                Log.d(TAG, "Error during file reference creating");
            }
            reportError();
            return;
        }

        if (isWriteToDestProvider) {
            destReference = Storage.createTempFile();
            if (destReference == null) {
                if (LOG) {
                    Log.w(TAG, "Error during file dest reference creating");
                }
                reportError();
                return;
            }
        }

        srcReference.openRead()
                .flatMap(f -> {
                    inputFile = f;
                    if (isWriteToDestProvider) {
                        return destReference.openWrite(srcReference.getSize());
                    } else {
                        return Promise.success(null);
                    }
                })
                .flatMap(f -> {
                    outputFile = f;

                    crc32 = new CRC32();

                    blocksCount = srcReference.getSize() / blockSize;
                    if (srcReference.getSize() % blockSize != 0) {
                        blocksCount++;
                    }

                    if (LOG) {
                        Log.d(TAG, "Starting uploading " + blocksCount + " blocks");
                        Log.d(TAG, "Requesting upload config...");
                    }

                    return api(new RequestGetFileUploadUrl(srcReference.getSize()));
                })
                .then(r -> {
                    if (LOG) {
                        Log.d(TAG, "Upload config loaded");
                    }
                    uploadConfig = r.getUploadKey();
                    checkQueue();
                })
                .failure(e -> {
                    if (LOG) {
                        Log.w(TAG, "Error during initialization of upload");
                    }
                    reportError();
                });
    }

    private void checkQueue() {
        if (isCompleted) {
            return;
        }

        if (nextBlock == blocksCount && uploadCount == 0) {
            if (LOG) {
                Log.d(TAG, "Completing...");
            }
            long crc = crc32.getValue();
            if (LOG) {
                Log.d(TAG, "Src #" + crc);

                Log.d(TAG, "Closing files...");
            }
            inputFile.close();
            if (isWriteToDestProvider) {
                outputFile.close();
            }

            request(new RequestCommitFileUpload(uploadConfig, fileName), new RpcCallback<ResponseCommitFileUpload>() {
                @Override
                public void onResult(ResponseCommitFileUpload response) {
                    if (LOG) {
                        Log.d(TAG, "Upload completed...");
                    }

                    FileReference location = new FileReference(response.getUploadedFileLocation(),
                            fileName, srcReference.getSize());

                    if (isWriteToDestProvider || alreadyInTemp) {
                        FileSystemReference reference = Storage.commitTempFile(alreadyInTemp ? srcReference : destReference, location.getFileId(),
                                location.getFileName());
                        reportComplete(location, reference);
                    } else {
                        reportComplete(location, srcReference);
                    }
                }

                @Override
                public void onError(RpcException e) {
                    if (LOG) {
                        Log.w(TAG, "Upload complete error");
                    }
                    reportError();
                }
            });
            return;
        }

        if (nextBlock < blocksCount && uploadCount < SIM_BLOCKS_COUNT) {
            loadPart(nextBlock++);
        }
    }

    private void loadPart(final int blockIndex) {
        int size = blockSize;
        int fileOffset = blockIndex * blockSize;
        if ((blockIndex + 1) * blockSize > srcReference.getSize()) {
            size = srcReference.getSize() - blockIndex * blockSize;
        }

        // TODO: Validate file part load ordering
        inputFile.read(fileOffset, size).then(filePart -> {
            if (isCompleted) {
                return;
            }
            if (LOG) {
                Log.d(TAG, "Block #" + blockIndex + " read");
            }

            if (isWriteToDestProvider) {
                if (!outputFile.write(fileOffset, filePart.getContents(), 0, filePart.getPartLength())) {
                    if (LOG) {
                        Log.w(TAG, "write #" + blockIndex + " error");
                    }
                    reportError();
                    return;
                }
            }

            crc32.update(filePart.getContents(), 0, filePart.getPartLength());

            if (LOG) {
                Log.d(TAG, "Starting block upload #" + blockIndex);
            }

            uploadCount++;
            uploadPart(blockIndex, filePart.getContents(), 0);
            checkQueue();
        }).failure(e -> {
            if (isCompleted) {
                return;
            }
            if (LOG) {
                Log.w(TAG, "Block #" + blockIndex + " read failure");
            }
            reportError();
        });
    }

    private void retryPart(int blockIndex, byte[] data, int attempt) {
        if (isCompleted) {
            return;
        }
        if (LOG) {
            Log.d(TAG, "Retrying block upload #" + blockIndex);
        }
        uploadPart(blockIndex, data, attempt);
    }

    private void uploadPart(final int blockIndex, final byte[] data, final int attempt) {
        api(new RequestGetFileUploadPartUrl(blockIndex, blockSize, uploadConfig))
                .flatMap(r -> HTTP.putMethod(r.getUrl(), data))
                .then(r -> {

                    if (LOG) {
                        Log.d(TAG, "Block #" + blockIndex + " uploaded");
                    }
                    uploadCount--;
                    uploaded++;

                    reportProgress(uploaded / (float) blocksCount);

                    checkQueue();

                })
                .failure(e -> {
                    if (e instanceof HTTPError) {
                        HTTPError httpError = (HTTPError) e;
                        if ((httpError.getErrorCode() >= 500 && httpError.getErrorCode() < 600) || httpError.getErrorCode() == 0) {
                            // Is Server Error or unknown error

                            int retryInSecs = DEFAULT_RETRY;

                            if (LOG) {
                                Log.w(TAG, "Block #" + blockIndex + " upload error #" + httpError.getErrorCode() + " trying again in " + retryInSecs + " sec, attempt #" + (attempt + 1));
                            }

                            schedule(new Retry(blockIndex, data, attempt + 1), retryInSecs * 1000L);
                            return;
                        }

                        if (LOG) {
                            Log.w(TAG, "Block #" + blockIndex + " upload failure");
                        }
                        reportError();
                    }
                });
    }

    private void reportError() {
        if (LOG) {
            Log.d(TAG, "Reporting error");
        }
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        manager.send(new UploadManager.UploadTaskError(rid));
    }

    private void reportProgress(float progress) {
        if (isCompleted) {
            return;
        }

        if (progress > currentProgress) {
            currentProgress = progress;
        }

        if (notifyCancellable != null) {
            notifyCancellable.cancel();
            notifyCancellable = null;
        }

        long delta = im.actor.runtime.Runtime.getActorTime() - lastNotifyDate;
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
        manager.send(new UploadManager.UploadTaskProgress(rid, currentProgress));
    }

    private void reportComplete(FileReference location, FileSystemReference reference) {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        manager.send(new UploadManager.UploadTaskComplete(rid, location, reference));
    }


    @Override
    public void onReceive(Object message) {
        if (message instanceof Retry) {
            Retry retry = (Retry) message;
            retryPart(retry.getBlockIndex(), retry.getData(), retry.getAttempt());
        } else {
            super.onReceive(message);
        }
    }

    private class Retry {

        private int blockIndex;
        private byte[] data;
        private int attempt;

        public Retry(int blockIndex, byte[] data, int attempt) {
            this.blockIndex = blockIndex;
            this.data = data;
            this.attempt = attempt;
        }

        public int getBlockIndex() {
            return blockIndex;
        }

        public byte[] getData() {
            return data;
        }

        public int getAttempt() {
            return attempt;
        }
    }
}
