package im.actor.model.android;

import android.content.Context;

import java.io.File;
import java.util.Random;

import im.actor.model.FileSystemProvider;
import im.actor.model.entity.FileLocation;
import im.actor.model.files.FileReference;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class AndroidFileProvider implements FileSystemProvider {

    private Context context;
    private Random random = new Random();
    private boolean isFirst = true;

    public AndroidFileProvider(Context context) {
        this.context = context;
    }

    private void clearTempDir() {
        File externalFile = context.getExternalFilesDir(null);
        if (externalFile == null) {
            return;
        }
        String externalPath = externalFile.getAbsolutePath();
        File dest = new File(externalPath + "/actor/tmp/");
        if (dest.exists()) {
            for (File file : dest.listFiles()) file.delete();
        }
    }

    private void checkTempDirs() {
        if (isFirst) {
            isFirst = false;
            clearTempDir();
        }
    }

    private String buildTempFile() {
        File externalFile = context.getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();
        File dest = new File(externalPath + "/actor/tmp/");
        dest.mkdirs();

        return new File(dest, "temp_" + random.nextLong()).getAbsolutePath();
    }

    private String buildResultFile(FileLocation fileLocation) {
        File externalFile = context.getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();
        File dest = new File(externalPath + "/actor/files/");
        dest.mkdirs();

        String baseFileName = fileLocation.getFileName();
        if (fileLocation.getFileName().contains(".")) {
            String prefix = baseFileName.substring(baseFileName.lastIndexOf('.'));
            String ext = baseFileName.substring(prefix.length() + 1);

            File res = new File(dest, prefix + "_" + fileLocation.getFileId() + "." + ext);
            int index = 0;
            while (res.exists()) {
                res = new File(dest, prefix + "_" + fileLocation.getFileId() + "_" + index + "." + ext);
                index++;
            }
            return res.getAbsolutePath();
        } else {
            File res = new File(dest, baseFileName + "_" + fileLocation.getFileId());
            int index = 0;
            while (res.exists()) {
                res = new File(dest, baseFileName + "_" + fileLocation.getFileId() + "_" + index);
                index++;
            }
            return res.getAbsolutePath();
        }
    }

    @Override
    public synchronized FileReference createTempFile(FileLocation fileLocation) {
        checkTempDirs();

        String destFile = buildTempFile();
        if (destFile == null) {
            return null;
        }
        return new AndroidFileReference(destFile);
    }

    @Override
    public synchronized FileReference commitTempFile(FileReference sourceFile, FileLocation fileLocation) {
        String fileName = buildResultFile(fileLocation);
        if (fileName == null) {
            return null;
        }

        if (!new File(sourceFile.getDescriptor()).renameTo(new File(fileName))) {
            return null;
        }
        return new AndroidFileReference(fileName);
    }

    @Override
    public boolean isFsPersistent() {
        return true;
    }

    @Override
    public synchronized FileReference fileFromDescriptor(String descriptor) {
        checkTempDirs();

        return new AndroidFileReference(descriptor);
    }
}
