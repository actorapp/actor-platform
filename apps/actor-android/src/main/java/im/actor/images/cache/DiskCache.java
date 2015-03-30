package im.actor.images.cache;

import im.actor.images.util.HashUtil;

import java.io.File;

/**
 * Created by ex3ndr on 27.08.14.
 */
public class DiskCache {

    private final String cachePath;

    public DiskCache(String cachePath) {
        this.cachePath = cachePath;
        new File(cachePath).mkdirs();
    }

    private String convertToFileName(String key) {
        return cachePath + HashUtil.md5(key) + ".cache";
    }

    private String convertToFileNameTemp(String key) {
        return cachePath + "/" + HashUtil.md5(key) + ".tcache";
    }

    public String lockFile(String key) {
        String fileName = convertToFileName(key);
        if (new File(fileName).exists()) {
            return fileName;
        }
        return null;
    }

    public void unlockFile(String key) {
        // DO Nothing
    }

    public void deleteFile(String key) {
        String fileName = convertToFileName(key);
        File file = new File(fileName);
        if (file.exists()) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }

    public String startWriteFile(String key) {
        String fileName = convertToFileNameTemp(key);
        File file = new File(fileName);
        if (file.exists()) {
            if (!file.delete()) {
                return null;
            }
        }
        return fileName;
    }

    public String commitFile(String key) {
        String sourceFileName = convertToFileNameTemp(key);
        String destFileName = convertToFileName(key);

        File source = new File(sourceFileName);
        File dest = new File(destFileName);
        if (!source.exists()) {
            return null;
        }
        if (source.renameTo(dest)) {
            return destFileName;
        }

        return null;
    }
}
