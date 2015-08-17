/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.file;

import im.actor.core.api.rpc.RequestCommitFileUpload;
import im.actor.core.api.rpc.RequestGetFileUploadPartUrl;
import im.actor.core.api.rpc.RequestGetFileUploadUrl;
import im.actor.core.api.rpc.ResponseCommitFileUpload;
import im.actor.core.api.rpc.ResponseGetFileUploadPartUrl;
import im.actor.core.api.rpc.ResponseGetFileUploadUrl;
import im.actor.core.entity.FileReference;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.HTTP;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.crypto.CRC32;
import im.actor.runtime.files.FileReadCallback;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.files.InputFile;
import im.actor.runtime.files.OutputFile;
import im.actor.runtime.http.FileUploadCallback;

public class UploadTask extends ModuleActor {

    private static final int SIM_BLOCKS_COUNT = 4;
    private static final int NOTIFY_THROTTLE = 1000;

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

    private byte[] uploadConfig;
    private CRC32 crc32;

    private float currentProgress;

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
        isWriteToDestProvider = Storage.isFsPersistent();

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

        inputFile = srcReference.openRead();
        if (inputFile == null) {
            if (LOG) {
                Log.w(TAG, "Error during file open");
            }
            reportError();
            return;
        }

        if (isWriteToDestProvider) {
            outputFile = destReference.openWrite(srcReference.getSize());
            if (outputFile == null) {
                inputFile.close();
                if (LOG) {
                    Log.w(TAG, "Error during dest file open");
                }
                reportError();
                return;
            }
        }

        crc32 = new CRC32();

        startUpload();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof NotifyProgress) {
            performReportProgress();
        } else {
            super.onReceive(message);
        }
    }

    private void startUpload() {
        blocksCount = srcReference.getSize() / blockSize;
        if (srcReference.getSize() % blockSize != 0) {
            blocksCount++;
        }

        if (LOG) {
            Log.d(TAG, "Starting uploading " + blocksCount + " blocks");
            Log.d(TAG, "Requesting upload config...");
        }

        request(new RequestGetFileUploadUrl(srcReference.getSize()),
                new RpcCallback<ResponseGetFileUploadUrl>() {
                    @Override
                    public void onResult(ResponseGetFileUploadUrl response) {
                        if (LOG) {
                            Log.d(TAG, "Upload config loaded");
                        }
                        uploadConfig = response.getUploadKey();
                        checkQueue();
                    }

                    @Override
                    public void onError(RpcException e) {
                        if (LOG) {
                            Log.w(TAG, "Upload config load error");
                        }
                        reportError();
                    }
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

                    if (isWriteToDestProvider) {
                        FileSystemReference reference = Storage.commitTempFile(destReference, location.getFileId(),
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
        byte[] data = new byte[size];

        final int finalSize = size;
        // TODO: Validate file part load ordering
        inputFile.read(fileOffset, data, 0, size, new FileReadCallback() {
            @Override
            public void onFileRead(final int fileOffset, final byte[] data, int offset, int len) {
                self().send(new Runnable() {
                    @Override
                    public void run() {
                        if (isCompleted) {
                            return;
                        }
                        if (LOG) {
                            Log.d(TAG, "Block #" + blockIndex + " read");
                        }

                        if (isWriteToDestProvider) {
                            if (!outputFile.write(fileOffset, data, 0, finalSize)) {
                                if (LOG) {
                                    Log.w(TAG, "write #" + blockIndex + " error");
                                }
                                reportError();
                                return;
                            }
                        }

                        crc32.update(data, 0, finalSize);

                        if (LOG) {
                            Log.d(TAG, "Starting block upload #" + blockIndex);
                        }

                        uploadCount++;
                        uploadPart(blockIndex, fileOffset, data);
                        checkQueue();
                    }
                });
            }

            @Override
            public void onFileReadError() {
                self().send(new Runnable() {
                    @Override
                    public void run() {
                        if (isCompleted) {
                            return;
                        }
                        if (LOG) {
                            Log.w(TAG, "Block #" + blockIndex + " read failure");
                        }
                        reportError();
                    }
                });
            }
        });
    }

    private void uploadPart(final int blockIndex, final int offset, final byte[] data) {
        request(new RequestGetFileUploadPartUrl(blockIndex, blockSize, uploadConfig),
                new RpcCallback<ResponseGetFileUploadPartUrl>() {
                    @Override
                    public void onResult(ResponseGetFileUploadPartUrl response) {
                        HTTP.putMethod(response.getUrl(), data, new FileUploadCallback() {
                            @Override
                            public void onUploaded() {
                                self().send(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (LOG) {
                                            Log.d(TAG, "Block #" + blockIndex + " uploaded");
                                        }
                                        uploadCount--;
                                        uploaded++;

                                        reportProgress(uploaded / (float) blocksCount);

                                        checkQueue();
                                    }
                                });
                            }

                            @Override
                            public void onUploadFailure() {
                                self().send(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (LOG) {
                                            Log.w(TAG, "Block #" + blockIndex + " upload failure");
                                        }
                                        reportError();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(RpcException e) {
                        if (LOG) {
                            Log.w(TAG, "Get Block #" + blockIndex + " url failure");
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
        manager.send(new UploadManager.UploadTaskProgress(rid, currentProgress));
    }

    private void reportComplete(FileReference location, FileSystemReference reference) {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        manager.send(new UploadManager.UploadTaskComplete(rid, location, reference));
    }

    private class NotifyProgress {

    }
}
