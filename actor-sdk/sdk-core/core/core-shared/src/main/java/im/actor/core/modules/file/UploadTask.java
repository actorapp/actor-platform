/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.file;

import im.actor.core.api.ApiDocumentEncryptionInfo;
import im.actor.core.api.rpc.RequestCommitFileUpload;
import im.actor.core.api.rpc.RequestGetFileUploadPartUrl;
import im.actor.core.api.rpc.RequestGetFileUploadUrl;
import im.actor.core.entity.FileReference;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.Crypto;
import im.actor.runtime.HTTP;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorCancellable;
import im.actor.runtime.crypto.BlockCipher;
import im.actor.runtime.crypto.CRC32;
import im.actor.runtime.crypto.primitives.modes.CBCBlockCipherStream;
import im.actor.runtime.crypto.primitives.util.ByteStrings;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.files.OutputFile;
import im.actor.runtime.files.SequenceFileSystemInputFile;
import im.actor.runtime.files.SequenceInputFile;
import im.actor.runtime.http.HTTPError;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.util.Hex;

public class UploadTask extends ModuleActor {

    // j2objc workaround
    private static final HTTPResponse DUMB = null;

    private static final int SIM_BLOCKS_COUNT = 4;
    private static final int NOTIFY_THROTTLE = 1000;
    private static final int DEFAULT_RETRY = 15;
    private static final int IV_SIZE = 16;

    private final String TAG;
    private final boolean LOG;

    private final long rid;
    private final String fileName;
    private final String descriptor;
    private final boolean isEncrypted;
    private BlockCipher encryptionCipher;
    private byte[] encryptionKey;
    private byte[] encryptionIv;

    private boolean isWriteToDestProvider = false;

    private FileSystemReference srcReference;
    private SequenceInputFile inputFile;

    private FileSystemReference destReference;
    private OutputFile outputFile;

    private ActorRef manager;
    private boolean isCompleted = false;

    private int finalSize;
    private int blockSize = 128 * 1024;
    private int blocksCount;
    private int nextBlock = 0;
    private int uploaded;
    private int uploadCount;
    private long lastNotifyDate;
    private ActorCancellable notifyCancellable;

    private byte[] uploadConfig;
    private CRC32 crc32;

    private float currentProgress;
    private boolean alreadyInTemp;

    public UploadTask(long rid, String descriptor, String fileName, boolean isEncrypted,
                      ActorRef manager, ModuleContext context) {
        super(context);
        this.LOG = context.getConfiguration().isEnableFilesLogging();
        this.rid = rid;
        this.fileName = fileName;
        this.descriptor = descriptor;
        this.isEncrypted = isEncrypted;
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

        srcReference.openRead().flatMap(f -> {
            inputFile = new SequenceFileSystemInputFile(f);
            if (isWriteToDestProvider) {
                return destReference.openWrite(srcReference.getSize());
            } else {
                return Promise.success(null);
            }
        }).flatMap(f -> {
            outputFile = f;

            // CRC
            crc32 = new CRC32();

            // File Size
            finalSize = srcReference.getSize();

            // Encryption
            if (isEncrypted) {
                encryptionKey = Crypto.randomBytes(32);
                encryptionIv = Crypto.randomBytes(16);

                Log.d(TAG, "File IV: " + Crypto.hex(encryptionIv));
                Log.d(TAG, "File Key: " + Crypto.hex(encryptionKey));

                encryptionCipher = new CBCBlockCipherStream(encryptionIv, Crypto.createAES256(encryptionKey));

                // Overwrite file size
                finalSize = 16/*IV*/ + Crypto.paddedLength(srcReference.getSize(), encryptionCipher.getBlockSize());
            }

            // Blocks
            blocksCount = finalSize / blockSize;
            if (finalSize % blockSize != 0) {
                blocksCount++;
            }

            if (LOG) {
                Log.d(TAG, "Starting uploading " + blocksCount + " blocks");
                Log.d(TAG, "Requesting upload config...");
            }

            return api(new RequestGetFileUploadUrl(finalSize));
        }).then(r -> {
            if (LOG) {
                Log.d(TAG, "Upload config loaded");
            }
            uploadConfig = r.getUploadKey();
            checkQueue();
        }).failure(e -> {
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

            api(new RequestCommitFileUpload(uploadConfig, fileName)).then(r -> {
                if (LOG) {
                    Log.d(TAG, "Upload completed...");
                }

                ApiDocumentEncryptionInfo encryptionInfo;
                if (isEncrypted) {
                    encryptionInfo = new ApiDocumentEncryptionInfo(srcReference.getSize(),
                            "aes128-hmac", encryptionKey);
                } else {
                    encryptionInfo = null;
                }

                FileReference location = new FileReference(r.getUploadedFileLocation(), fileName,
                        finalSize, encryptionInfo);

                if (isWriteToDestProvider || alreadyInTemp) {
                    FileSystemReference reference = Storage.commitTempFile(alreadyInTemp ? srcReference : destReference, location.getFileId(),
                            location.getFileName());
                    reportComplete(location, reference);
                } else {
                    reportComplete(location, srcReference);
                }
            }).failure(e -> {
                if (LOG) {
                    Log.w(TAG, "Upload complete error");
                }
                reportError();
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

        // Calculating appropriate read block size
        if (isEncrypted) {
            if (blockIndex == 0) {
                if (srcReference.getSize() - IV_SIZE > blockSize) {
                    size = blockSize - IV_SIZE;
                } else {
                    size = srcReference.getSize();
                }
            } else {
                if ((blockIndex + 1) * blockSize - IV_SIZE > srcReference.getSize()) {
                    size = srcReference.getSize() - blockIndex * blockSize;
                }
            }
        } else {
            if ((blockIndex + 1) * blockSize > srcReference.getSize()) {
                size = srcReference.getSize() - blockIndex * blockSize;
            }
        }

        inputFile.readBlock(size).then(filePart -> {
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

            // Block Encryption
            if (isEncrypted) {
                // Result Block
                int destBlockCount = (int) Math.ceil(filePart.getContents().length /
                        (float) encryptionCipher.getBlockSize());
                int destBlockSize = destBlockCount * encryptionCipher.getBlockSize();
                int destBlockOffset = 0;
                if (blockIndex == 0) {
                    destBlockOffset = 16;
                }

                // Block for uploading
                byte[] res = new byte[destBlockSize + destBlockOffset];

                // Appending IV if needed
                if (blockIndex == 0) {
                    for (int i = 0; i < IV_SIZE; i++) {
                        res[i] = encryptionIv[i];
                    }
                }

                // Encrypting Block
                for (int i = 0; i < destBlockCount; i++) {
                    if (i == destBlockCount - 1) {
                        byte[] tmp = new byte[encryptionCipher.getBlockSize()];
                        for (int j = 0; j < encryptionCipher.getBlockSize() && i * encryptionCipher.getBlockSize() + j < filePart.getContents().length; j++) {
                            tmp[j] = filePart.getContents()[j];
                        }
                        encryptionCipher.encryptBlock(tmp, 0, res, destBlockOffset + i * encryptionCipher.getBlockSize());
                    } else {
                        encryptionCipher.encryptBlock(filePart.getContents(), i * encryptionCipher.getBlockSize(),
                                res, destBlockOffset + i * encryptionCipher.getBlockSize());
                    }
                }

                uploadPart(blockIndex, res, 0);
            } else {
                uploadPart(blockIndex, filePart.getContents(), 0);
            }
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
