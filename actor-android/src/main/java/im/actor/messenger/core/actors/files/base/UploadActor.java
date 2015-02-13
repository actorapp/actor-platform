package im.actor.messenger.core.actors.files.base;

import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.tasks.AskTimeoutException;
import com.droidkit.actors.tasks.TaskActor;

import im.actor.api.scheme.UploadConfig;
import im.actor.api.scheme.rpc.ResponseCompleteUpload;
import im.actor.api.scheme.rpc.ResponseStartUpload;
import im.actor.api.scheme.rpc.ResponseVoid;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.encryption.FileEncryption;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.util.Logger;
import im.actor.messenger.util.io.StreamingUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;
import java.util.zip.CRC32;

import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class UploadActor extends TaskActor<FileLocation> {

    public static ActorSelection upload(final String fileName, final boolean isEncrypted) {
        return new ActorSelection(Props.create(UploadActor.class, new ActorCreator<UploadActor>() {
            @Override
            public UploadActor create() {
                return new UploadActor(isEncrypted, fileName);
            }
        }), "upload_" + UUID.randomUUID());
    }

    private static final String TAG = "UploadActor";

    private static final int BLOCKS_COUNT = 4;

    private boolean useEncryption;
    private byte[] encryptionKey;

    private int sourceSize;
    private String sourceFileName;

    private int uploadSize;
    private String uploadFileName;

    private RandomAccessFile randomAccessFile;
    private int blockSize = 8 * 1024;
    private int blocksCount;
    private int nextBlock = 0;

    private UploadConfig uploadConfig;
    private int uploaded;
    private int uploadCount;
    private CRC32 crc32;
    private long start;

    public UploadActor(boolean useEncryption, String fileName) {
        this.useEncryption = useEncryption;
        this.sourceFileName = fileName;
    }

    @Override
    public void startTask() {
        if (useEncryption) {
            uploadFileName = AppContext.getExternalTempFile("enc", "bin");
            ask(FileEncryption.fileEncryption().encryptFile(sourceFileName, uploadFileName), new FutureCallback<byte[]>() {
                @Override
                public void onResult(byte[] bytes) {
                    encryptionKey = bytes;
                    startUpload();
                }

                @Override
                public void onError(Throwable throwable) {
                    error(throwable);
                }
            });
        } else {
            uploadFileName = sourceFileName;
            startUpload();
        }
    }

    private void startUpload() {
        if (isCompleted()) {
            return;
        }

        Logger.d(TAG, "Starting upload");
        start = System.currentTimeMillis();

        sourceSize = (int) new File(sourceFileName).length();
        uploadSize = (int) new File(uploadFileName).length();

        try {
            randomAccessFile = new RandomAccessFile(uploadFileName, "r");
        } catch (FileNotFoundException e) {
            error(e);
            return;
        }

        Logger.d(TAG, "File size: " + uploadSize + " bytes");

        blocksCount = uploadSize / blockSize;
        if (uploadSize % blockSize != 0) {
            blocksCount++;
        }

        crc32 = new CRC32();

        ask(requests().startUpload(), new FutureCallback<ResponseStartUpload>() {
            @Override
            public void onResult(ResponseStartUpload result) {
                Logger.d(TAG, "Started upload");
                UploadActor.this.uploadConfig = result.getConfig();
                checkUpload();
            }

            @Override
            public void onError(Throwable throwable) {
                Logger.d(TAG, "Error");
                error(throwable);
            }
        });
    }

    private void checkUpload() {
        Logger.d(TAG, "checkUpload " + nextBlock + "/" + blocksCount + " - " + uploadCount);
        if (nextBlock == blocksCount && uploadCount == 0) {
            Logger.d(TAG, "Completing...");
            long crc = crc32.getValue();
            Logger.d(TAG, "Src #" + crc);
            ask(requests().completeUpload(uploadConfig, blocksCount, crc), new FutureCallback<ResponseCompleteUpload>() {
                @Override
                public void onResult(ResponseCompleteUpload result) {
                    Logger.d(TAG, "Completed in " + (System.currentTimeMillis() - start) + " ms");

                    FileLocation location;
                    if (useEncryption) {
                        location = new FileLocation(result.getLocation().getFileId(), result.getLocation().getAccessHash(),
                                uploadSize, FileLocation.Encryption.AES_THEN_MAC, sourceSize, encryptionKey);
                    } else {
                        location = new FileLocation(result.getLocation().getFileId(), result.getLocation().getAccessHash(),
                                uploadSize);
                    }

                    complete(location);
                }

                @Override
                public void onError(Throwable throwable) {
                    Logger.d(TAG, "Error");
                    error(throwable);
                }
            });
            return;
        }

        if (nextBlock < blocksCount && uploadCount < BLOCKS_COUNT) {

            final int blockIndex = nextBlock++;

            byte[] data;
            int size = blockSize;
            try {
                randomAccessFile.seek(blockIndex * blockSize);
                if ((blockIndex + 1) * blockSize > randomAccessFile.length()) {
                    size = (int) (randomAccessFile.length() - blockIndex * blockSize);
                }
                data = StreamingUtils.readBytes(size, randomAccessFile);
            } catch (IOException e) {
                error(e);
                return;
            }

            crc32.update(data, 0, size);

            Logger.d(TAG, "Starting block upload #" + blockIndex);

            uploadCount++;

            performUpload(blockIndex, blockIndex * blockSize, data);
            checkUpload();
        }
    }

    private void performUpload(final int blockIndex, final int offset, final byte[] data) {
        ask(requests().uploadPart(uploadConfig, offset, data), new FutureCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid result) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        ask(requests().uploadPart(uploadConfig, offset, data), new FutureCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid result) {
                Logger.d(TAG, "Block #" + blockIndex + " uploaded");
                uploadCount--;
                uploaded++;
                progress(100 * uploaded / blocksCount);
                checkUpload();
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable instanceof AskTimeoutException) {
                    Logger.d(TAG, "Block #" + blockIndex + " error. Trying again...");
                    performUpload(blockIndex, offset, data);
                } else {
                    error(throwable);
                }
            }
        });
    }

    @Override
    public void onTaskObsolete() {

    }
}
