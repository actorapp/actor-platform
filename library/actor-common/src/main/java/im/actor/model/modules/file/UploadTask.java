package im.actor.model.modules.file;

import im.actor.model.api.UploadConfig;
import im.actor.model.api.rpc.RequestCompleteUpload;
import im.actor.model.api.rpc.RequestStartUpload;
import im.actor.model.api.rpc.RequestUploadPart;
import im.actor.model.api.rpc.ResponseCompleteUpload;
import im.actor.model.api.rpc.ResponseStartUpload;
import im.actor.model.api.rpc.ResponseVoid;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;
import im.actor.model.files.InputFile;
import im.actor.model.files.OutputFile;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.entity.EntityConverter;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.util.CRC32;

/**
 * Created by ex3ndr on 03.03.15.
 */
public class UploadTask extends ModuleActor {

    private static final int SIM_BLOCKS_COUNT = 4;

    private final String TAG;
    private final boolean LOG;

    private long rid;
    private String fileName;
    private String descriptor;

    private FileSystemReference srcReference;
    private InputFile inputFile;

    private FileSystemReference destReference;
    private OutputFile outputFile;

    private ActorRef manager;
    private boolean isCompleted = false;

    private int blockSize = 8 * 1024;
    private int blocksCount;
    private int nextBlock = 0;
    private int uploaded;
    private int uploadCount;

    private UploadConfig uploadConfig;
    private CRC32 crc32;

    public UploadTask(long rid, String descriptor, String fileName, ActorRef manager, Modules modules) {
        super(modules);
        this.LOG = modules.getConfiguration().isEnableFilesLogging();
        this.rid = rid;
        this.fileName = fileName;
        this.descriptor = descriptor;
        this.manager = manager;
        this.TAG = "UploadTask{" + rid + "}";
    }

    @Override
    public void preStart() {
        srcReference = config().getFileSystemProvider().fileFromDescriptor(descriptor);
        if (srcReference == null) {
            if (LOG) {
                Log.d(TAG, "Error during file reference creating");
            }
            reportError();
            return;
        }

        destReference = config().getFileSystemProvider().createTempFile();
        if (destReference == null) {
            if (LOG) {
                Log.d(TAG, "Error during file dest reference creating");
            }
            reportError();
            return;
        }

        inputFile = srcReference.openRead();
        if (inputFile == null) {
            if (LOG) {
                Log.d(TAG, "Error during file open");
            }
            reportError();
            return;
        }

        outputFile = destReference.openWrite(srcReference.getSize());
        if (outputFile == null) {
            inputFile.close();
            if (LOG) {
                Log.d(TAG, "Error during dest file open");
            }
            reportError();
            return;
        }

        crc32 = new CRC32();

        startUpload();
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
        request(new RequestStartUpload(), new RpcCallback<ResponseStartUpload>() {
            @Override
            public void onResult(ResponseStartUpload response) {
                if (LOG) {
                    Log.d(TAG, "Upload config loaded");
                }
                uploadConfig = response.getConfig();
                checkQueue();
            }

            @Override
            public void onError(RpcException e) {
                if (LOG) {
                    Log.d(TAG, "Upload config load error");
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
            outputFile.close();

            request(new RequestCompleteUpload(uploadConfig, blocksCount, crc), new RpcCallback<ResponseCompleteUpload>() {
                @Override
                public void onResult(ResponseCompleteUpload response) {
                    if (LOG) {
                        Log.d(TAG, "Upload completed...");
                    }

                    FileReference location = EntityConverter.convert(response.getLocation(), fileName, srcReference.getSize());

                    FileSystemReference reference = config().getFileSystemProvider().commitTempFile(destReference, location);

                    reportComplete(location, reference);
                }

                @Override
                public void onError(RpcException e) {
                    if (LOG) {
                        Log.d(TAG, "Upload complete error");
                    }
                    reportError();
                }
            });
            return;
        }

        if (nextBlock < blocksCount && uploadCount < SIM_BLOCKS_COUNT) {
            final int blockIndex = nextBlock++;

            int size = blockSize;
            int fileOffset = blockIndex * blockSize;
            if ((blockIndex + 1) * blockSize > srcReference.getSize()) {
                size = srcReference.getSize() - blockIndex * blockSize;
            }
            byte[] data = new byte[size];

            if (!inputFile.read(fileOffset, data, 0, size)) {
                if (LOG) {
                    Log.d(TAG, "read #" + blockIndex + " error");
                }
                reportError();
                return;
            }
            if (!outputFile.write(fileOffset, data, 0, size)) {
                if (LOG) {
                    Log.d(TAG, "write #" + blockIndex + " error");
                }
                reportError();
                return;
            }

            crc32.update(data, 0, size);

            if (LOG) {
                Log.d(TAG, "Starting block upload #" + blockIndex);
            }

            uploadCount++;
            uploadPart(blockIndex, fileOffset, data);
            checkQueue();
        } else {
            if (LOG) {
                Log.d(TAG, "Nothing to do");
            }
        }
    }

    private void uploadPart(final int blockIndex, final int offset, final byte[] data) {
        request(new RequestUploadPart(uploadConfig, offset, data), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                if (LOG) {
                    Log.d(TAG, "Block #" + blockIndex + " uploaded");
                }
                uploadCount--;
                uploaded++;

                reportProgress(uploaded / (float) blocksCount);

                checkQueue();
            }

            @Override
            public void onError(RpcException e) {
                reportError();
            }
        });
    }

    private void reportError() {
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
        manager.send(new UploadManager.UploadTaskProgress(rid, progress));
    }

    private void reportComplete(FileReference location, FileSystemReference reference) {
        if (isCompleted) {
            return;
        }
        isCompleted = true;
        manager.send(new UploadManager.UploadTaskComplete(rid, location, reference));
    }
}
