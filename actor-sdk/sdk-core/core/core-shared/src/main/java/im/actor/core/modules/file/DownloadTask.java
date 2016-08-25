/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.file;

import im.actor.core.entity.FileReference;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.Crypto;
import im.actor.runtime.HTTP;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorCancellable;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.crypto.BlockCipher;
import im.actor.runtime.crypto.primitives.modes.CBCBlockCipherStream;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.files.OutputFile;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.function.Supplier;
import im.actor.runtime.http.HTTPError;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.Promises;

public class DownloadTask extends ModuleActor {

    // j2objc workaround
    private static final Void DUMB = null;

    private static final int SIM_BLOCKS_COUNT = 4;
    private static final int NOTIFY_THROTTLE = 1000;
    private static final int DEFAULT_RETRY = 15;

    private final String TAG;
    private final boolean LOG;

    private FileReference fileReference;
    private BlockCipher encryptionCipher;
    private ActorRef manager;

    private FileSystemReference destReference;
    private OutputFile outputFile;

    private boolean isCompleted;

    private long lastNotifyDate;
    private float currentProgress;
    private ActorCancellable notifyCancellable;

    private String fileUrl;
    private int blockSize = 128 * 1024;
    private int blocksCount;

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

        Promises.traverseParallel(SIM_BLOCKS_COUNT, partsSupplier(), new Consumer<HTTPResponse>() {

            int index = 0;

            @Override
            public void apply(HTTPResponse r) {
                reportProgress((index + 1) / (float) blocksCount);

                if (fileReference.getEncryptionInfo() != null) {

                    int offset = 0;

                    if (index == 0) {
                        byte[] iv = ByteStrings.substring(r.getContent(), 0, 16);
                        byte[] key = ByteStrings.substring(fileReference.getEncryptionInfo().getKey(), 0, 32);
                        Log.d(TAG, "File IV: " + Crypto.hex(iv));
                        Log.d(TAG, "File Key: " + Crypto.hex(key));
                        encryptionCipher = new CBCBlockCipherStream(iv, Crypto.createAES256(key));
                        offset = 16;
                    }

                    byte[] dest = new byte[r.getContent().length - offset];
                    for (int i = 0; i < (dest.length - offset) / encryptionCipher.getBlockSize(); i++) {
                        encryptionCipher.decryptBlock(r.getContent(), offset + i * encryptionCipher.getBlockSize(),
                                dest, i * encryptionCipher.getBlockSize());
                    }

                    if (index == 0) {
                        if (!outputFile.write(index * blockSize, dest, 0, dest.length)) {
                            throw new RuntimeException("Unable to write file");
                        }
                    } else {
                        if (!outputFile.write(index * blockSize - 16, dest, 0, dest.length)) {
                            throw new RuntimeException("Unable to write file");
                        }
                    }
                } else {
                    if (!outputFile.write(index * blockSize, r.getContent(), 0, r.getContent().length)) {
                        throw new RuntimeException("Unable to write file");
                    }
                }

                index++;
            }
        }).then(r -> {
            completeDownload();
        }).failure(e -> {
            completeWithError();
        });
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

    private void completeWithError() {
        reportError();
    }

    // Downloading parts

    private Supplier<Promise<HTTPResponse>> partsSupplier() {
        return new Supplier<Promise<HTTPResponse>>() {
            int nextBlock = 0;

            @Override
            public Promise<HTTPResponse> get() {
                if (nextBlock >= blocksCount) {
                    return null;
                }
                int blockIndex = nextBlock++;
                int offset = blockIndex * blockSize;
                return downloadPartPromise(blockIndex, offset, 0);
            }
        };
    }

    private Promise<HTTPResponse> downloadPartPromise(int blockIndex, int fileOffset, int attempt) {
        return new Promise<>((PromiseFunc<HTTPResponse>) resolver -> {
            HTTP.getMethod(fileUrl, fileOffset, blockSize, fileReference.getFileSize()).then(r -> {
                if (LOG) {
                    Log.d(TAG, "Download part #" + blockIndex + " completed");
                }
                resolver.result(r);
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

                    schedule((Runnable) () -> {
                        downloadPartPromise(blockIndex, fileOffset, attempt + 1).pipeTo(resolver);
                    }, retryInSecs * 1000L);
                } else {
                    if (LOG) {
                        Log.d(TAG, "Download part #" + blockIndex + " failure");
                    }
                    resolver.error(e);
                }
            });
        });
    }

    // Reporting

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
}
