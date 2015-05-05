/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android;

import android.content.Context;

import java.io.File;
import java.util.Random;

import im.actor.model.FileSystemProvider;
import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;

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

    private String buildResultFile(FileReference fileReference) {
        File externalFile = context.getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();
        File dest = new File(externalPath + "/actor/files/");
        dest.mkdirs();

        String baseFileName = fileReference.getFileName();
        if (fileReference.getFileName().contains(".")) {
            String prefix = baseFileName.substring(baseFileName.lastIndexOf('.'));
            String ext = baseFileName.substring(prefix.length() + 1);

            File res = new File(dest, prefix + "_" + fileReference.getFileId() + "." + ext);
            int index = 0;
            while (res.exists()) {
                res = new File(dest, prefix + "_" + fileReference.getFileId() + "_" + index + "." + ext);
                index++;
            }
            return res.getAbsolutePath();
        } else {
            File res = new File(dest, baseFileName + "_" + fileReference.getFileId());
            int index = 0;
            while (res.exists()) {
                res = new File(dest, baseFileName + "_" + fileReference.getFileId() + "_" + index);
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
    public synchronized FileSystemReference commitTempFile(FileSystemReference sourceFile, FileReference fileReference) {
        String fileName = buildResultFile(fileReference);
        if (fileName == null) {
            return null;
        }

        if (!new File(sourceFile.getDescriptor()).renameTo(new File(fileName))) {
            return null;
        }
        return new AndroidFileSystemReference(fileName);
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
