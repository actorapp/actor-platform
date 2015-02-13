package im.actor.messenger.core.actors.files.base;

import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.tasks.AskTimeoutException;
import com.droidkit.actors.tasks.TaskActor;

import im.actor.api.scheme.rpc.ResponseGetFile;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.encryption.FileEncryption;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.util.Logger;

import java.io.*;

import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 06.09.14.
 */
public class DownloadActor extends TaskActor<String> {

    public static ActorSelection download(final FileLocation location, final String destFile) {
        return new ActorSelection(Props.create(DownloadActor.class, new ActorCreator<DownloadActor>() {
            @Override
            public DownloadActor create() {
                return new DownloadActor(location, destFile);
            }
        }), "downloader_" + location.getFileId());
    }

    private final String TAG;

    private static final int BLOCKS_COUNT = 4;

    private FileLocation fileLocation;

    private String destFileName;
    private String downloadFileName;

    private RandomAccessFile randomAccessFile;
    private int blockSize = 8 * 1024;
    private int blocksCount;
    private int nextBlock = 0;
    private int currentDownloads = 0;
    private int downloaded = 0;

    public DownloadActor(FileLocation fileLocation, String destFileName) {
        TAG = "DownloadManager:Actor:" + fileLocation.getFileId();
        this.fileLocation = fileLocation;
        this.destFileName = destFileName;
    }

    @Override
    public void startTask() {
        Logger.d(TAG, "Starting task");

        if (fileLocation.getEncryption() == FileLocation.Encryption.NONE) {
            downloadFileName = destFileName;
        } else {
            downloadFileName = AppContext.getExternalTempFile("enc", "bin");
        }

        startDownload();
    }

    private void startDownload() {
        try {
            new File(downloadFileName).delete();
            randomAccessFile = new RandomAccessFile(downloadFileName, "rws");
            randomAccessFile.setLength(fileLocation.getFileSize());
        } catch (FileNotFoundException e) {
            error(e);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            error(e);
            return;
        }

        blocksCount = fileLocation.getFileSize() / blockSize;
        if (fileLocation.getFileSize() % blockSize != 0) {
            blocksCount++;
        }

        Logger.d(TAG, "Downloading " + blocksCount + " blocks");

        checkDownload();
    }

    private void completeDownload() {
        Logger.d(TAG, "Completing download");
        try {
            randomAccessFile.getFD().sync();
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            error(e);
            return;
        }

        if (fileLocation.getEncryption() == FileLocation.Encryption.NONE) {
            complete(destFileName);
        } else if (fileLocation.getEncryption() == FileLocation.Encryption.AES_THEN_MAC) {
            ask(FileEncryption.fileEncryption().decryptFile(downloadFileName, fileLocation.getEncryptionKey(),
                    destFileName), new FutureCallback<Boolean>() {
                @Override
                public void onResult(Boolean aBoolean) {
                    complete(destFileName);
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                    error(throwable);
                }
            });
        } else if (fileLocation.getEncryption() == FileLocation.Encryption.AES) {
            ask(FileEncryption.fileEncryption().decryptFileAes(downloadFileName, fileLocation.getEncryptionKey(),
                    destFileName), new FutureCallback<Boolean>() {
                @Override
                public void onResult(Boolean aBoolean) {
                    complete(destFileName);
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                    error(throwable);
                }
            });
        } else {
            error(new RuntimeException("Unsupported encryption type"));
        }
    }

    private void checkDownload() {
        Logger.d(TAG, "checkDownload " + currentDownloads + "/" + nextBlock);
        if (currentDownloads == 0 && nextBlock >= blocksCount) {
            completeDownload();
        } else if (currentDownloads < BLOCKS_COUNT && nextBlock < blocksCount) {
            currentDownloads++;
            final int blockIndex = nextBlock++;
            final int offset = blockIndex * blockSize;

            Logger.d(TAG, "Starting part #" + blockIndex + " download");

            performDownload(blockIndex, offset, new im.actor.api.scheme.FileLocation(fileLocation.getFileId(),
                    fileLocation.getAccessHash()));

            checkDownload();
        }
    }

    private void performDownload(final int blockIndex, final int offset, final im.actor.api.scheme.FileLocation location) {
        ask(requests().getFile(location, offset, blockSize), new FutureCallback<ResponseGetFile>() {
            @Override
            public void onResult(ResponseGetFile result) {
                downloaded++;
                Logger.d(TAG, "Download part #" + blockIndex + " completed");
                try {
                    randomAccessFile.seek(offset);
                    randomAccessFile.write(result.getPayload());
                } catch (IOException e) {
                    e.printStackTrace();
                    error(e);
                    return;
                }
                currentDownloads--;
                progress(100 * downloaded / blocksCount);
                checkDownload();
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable instanceof AskTimeoutException) {
                    Logger.d(TAG, "Download part #" + blockIndex + " error");
                    performDownload(blockIndex, offset, location);
                } else {
                    Logger.d(TAG, "Download part #" + blockIndex + " error: aborting");
                    error(throwable);
                }
            }
        });
    }
}
