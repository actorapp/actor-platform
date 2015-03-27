package im.actor.model.modules.file;

import im.actor.model.FileSystemProvider;
import im.actor.model.api.rpc.RequestGetFile;
import im.actor.model.api.rpc.ResponseGetFile;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;
import im.actor.model.files.OutputFile;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class DownloadTask extends ModuleActor {

    private static final int SIM_BLOCKS_COUNT = 4;

    private final String TAG;

    private FileReference fileReference;
    private ActorRef manager;
    private FileSystemProvider fileSystemProvider;

    private FileSystemReference destReference;
    private OutputFile outputFile;

    private boolean isCompleted;

    private int blockSize = 8 * 1024;
    private int blocksCount;
    private int nextBlock = 0;
    private int currentDownloads = 0;
    private int downloaded = 0;

    public DownloadTask(FileReference fileReference, ActorRef manager, Modules messenger) {
        super(messenger);
        this.TAG = "DownloadTask{" + fileReference.getFileId() + "}";
        this.fileReference = fileReference;
        this.manager = manager;
    }

    @Override
    public void preStart() {

        Log.d(TAG, "Creating file...");

        fileSystemProvider = modules().getConfiguration().getFileSystemProvider();
        if (fileSystemProvider == null) {
            reportError();
            Log.d(TAG, "No FileSystem available");
            return;
        }

        destReference = fileSystemProvider.createTempFile();
        if (destReference == null) {
            reportError();
            Log.d(TAG, "Unable to create reference");
            return;
        }

        outputFile = destReference.openWrite(fileReference.getFileSize());
        if (outputFile == null) {
            reportError();
            Log.d(TAG, "Unable to write wile");
            return;
        }

        startDownload();
    }

    private void startDownload() {
        blocksCount = fileReference.getFileSize() / blockSize;
        if (fileReference.getFileSize() % blockSize != 0) {
            blocksCount++;
        }

        Log.d(TAG, "Starting downloading " + blocksCount + " blocks");
        checkQueue();
    }

    private void completeDownload() {
        if (isCompleted) {
            return;
        }

        Log.d(TAG, "Closing file...");
        if (!outputFile.close()) {
            reportError();
            return;
        }

        FileSystemReference reference = fileSystemProvider.commitTempFile(destReference, fileReference);
        if (reference == null) {
            reportError();
            return;
        }

        Log.d(TAG, "Complete download {" + reference.getDescriptor() + "}");
        reportComplete(reference);
    }

    private void checkQueue() {
        if (isCompleted) {
            return;
        }

        Log.d(TAG, "checkQueue " + currentDownloads + "/" + nextBlock);
        if (currentDownloads == 0 && nextBlock >= blocksCount) {
            completeDownload();
        } else if (currentDownloads < SIM_BLOCKS_COUNT && nextBlock < blocksCount) {
            currentDownloads++;
            int blockIndex = nextBlock++;
            int offset = blockIndex * blockSize;

            Log.d(TAG, "Starting part #" + blockIndex + " download");

            downloadPart(blockIndex, offset);

            checkQueue();
        } else {
            Log.d(TAG, "Task queue is full");
        }
    }

    private void downloadPart(final int blockIndex, final int fileOffset) {
        request(new RequestGetFile(new im.actor.model.api.FileLocation(fileReference.getFileId(),
                fileReference.getAccessHash()), fileOffset, blockSize), new RpcCallback<ResponseGetFile>() {
            @Override
            public void onResult(ResponseGetFile response) {
                downloaded++;
                Log.d(TAG, "Download part #" + blockIndex + " completed");
                if (!outputFile.write(fileOffset, response.getPayload(), 0,
                        response.getPayload().length)) {
                    reportError();
                    return;
                }
                currentDownloads--;
                reportProgress(downloaded / (float) blocksCount);
                checkQueue();
            }

            @Override
            public void onError(RpcException e) {
                Log.d(TAG, "Download part #" + blockIndex + " failure");
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
