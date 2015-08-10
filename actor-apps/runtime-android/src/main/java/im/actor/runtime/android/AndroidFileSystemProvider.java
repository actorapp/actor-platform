/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android;

import java.io.File;
import java.util.Random;

import im.actor.runtime.FileSystemRuntime;
import im.actor.runtime.android.files.AndroidFileSystemReference;
import im.actor.runtime.files.FileSystemReference;

public class AndroidFileSystemProvider implements FileSystemRuntime {

    private Random random = new Random();
    private boolean isFirst = true;

    private void clearTempDir() {
        File externalFile = AndroidContext.getContext().getExternalFilesDir(null);
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
        File externalFile = AndroidContext.getContext().getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();
        File dest = new File(externalPath + "/actor/tmp/");
        dest.mkdirs();

        return new File(dest, "temp_" + random.nextLong()).getAbsolutePath();
    }

    private String buildResultFile(long fileId, String fileName) {
        File externalFile = AndroidContext.getContext().getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();
        File dest = new File(externalPath + "/actor/files/");
        dest.mkdirs();

        String baseFileName = fileName;
        if (fileName.contains(".")) {
            String prefix = baseFileName.substring(baseFileName.lastIndexOf('.'));
            String ext = baseFileName.substring(prefix.length() + 1);

            File res = new File(dest, prefix + "_" + fileId + "." + ext);
            int index = 0;
            while (res.exists()) {
                res = new File(dest, prefix + "_" + fileId + "_" + index + "." + ext);
                index++;
            }
            return res.getAbsolutePath();
        } else {
            File res = new File(dest, baseFileName + "_" + fileId);
            int index = 0;
            while (res.exists()) {
                res = new File(dest, baseFileName + "_" + fileId + "_" + index);
                index++;
            }
            return res.getAbsolutePath();
        }
    }

    @Override
    public synchronized FileSystemReference createTempFile() {
        checkTempDirs();

        String destFile = buildTempFile();
        if (destFile == null) {
            return null;
        }
        return new AndroidFileSystemReference(destFile);
    }

    @Override
    public FileSystemReference commitTempFile(FileSystemReference sourceFile, long fileId, String fileName) {
        String realFileName = buildResultFile(fileId, fileName);
        if (realFileName == null) {
            return null;
        }

        if (!new File(sourceFile.getDescriptor()).renameTo(new File(realFileName))) {
            return null;
        }
        return new AndroidFileSystemReference(realFileName);
    }

    @Override
    public boolean isFsPersistent() {
        return true;
    }

    @Override
    public synchronized FileSystemReference fileFromDescriptor(String descriptor) {
        checkTempDirs();

        return new AndroidFileSystemReference(descriptor);
    }
}
