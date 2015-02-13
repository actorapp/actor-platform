package im.actor.messenger.core.actors.send.video;

import com.droidkit.actors.Actor;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.util.RandomUtil;
import im.actor.messenger.util.io.StreamingUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by ex3ndr on 20.09.14.
 */
public class FilePipelineActor extends Actor {

    private RandomAccessFile randomAccessFile;
    private int available;
    private boolean isCompleted;

    @Override
    public void preStart() {
        super.preStart();

        File externalFile = AppContext.getContext().getExternalFilesDir(null);
        if (externalFile == null) {
            return;
        }
        String externalPath = externalFile.getAbsolutePath();

        File dest = new File(externalPath + "/Reactive/");
        dest.mkdirs();

        final File outputFile = new File(dest, "upload_" + RandomUtil.randomId() + ".jpg");
        final String fileName = outputFile.getAbsolutePath();

        try {
            randomAccessFile = new RandomAccessFile(fileName, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            context().stopSelf();
            return;
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof FilePart) {
            if (isCompleted) {
                drop(message);
                return;
            }
            byte[] part = ((FilePart) message).getPart();
            if (part.length == 0) {
                return;
            }
            try {
                randomAccessFile.seek(available);
                randomAccessFile.write(part);
                available += part.length;
            } catch (IOException e) {
                e.printStackTrace();
                context().stopSelf();
                return;
            }
            self().sendOnce(new FileAvailableNotify());
        } else if (message instanceof FileEnd) {
            if (isCompleted) {
                drop(message);
                return;
            }
            isCompleted = true;
            self().sendOnce(new FileEndNotify());
        } else if (message instanceof FileAvailableNotify) {
            onPipeAvailable(available);
        } else if (message instanceof FileEndNotify) {
            onPipeCompleted();
        }
    }

    @Override
    public void postStop() {
        super.postStop();
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public int getAvailable() {
        return available;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    protected void onPipeAvailable(int available) {

    }

    protected void onPipeCompleted() {

    }

    protected byte[] readPart(int offset, int limit) throws IOException {
        if (offset >= available) {
            return new byte[0];
        }
        int size = Math.min(available - offset, limit);
        if (size == 0) {
            return new byte[0];
        }
        return StreamingUtils.readBytes(size, randomAccessFile);
    }


    private static class FileAvailableNotify {
    }

    private static class FileEndNotify {
    }

    public static class FilePart {
        public byte[] part;

        public FilePart(byte[] part) {
            this.part = part;
        }

        public byte[] getPart() {
            return part;
        }
    }

    public static class FileEnd {

    }
}
