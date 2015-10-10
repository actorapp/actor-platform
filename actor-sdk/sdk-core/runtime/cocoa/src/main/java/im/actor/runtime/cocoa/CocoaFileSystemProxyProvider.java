package im.actor.runtime.cocoa;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.FileSystemRuntime;
import im.actor.runtime.files.FileSystemReference;

public class CocoaFileSystemProxyProvider implements FileSystemRuntime {

    private static FileSystemRuntime runtime;

    @ObjectiveCName("setFileSystemRuntime:")
    public static void setFileSystemRuntime(FileSystemRuntime runtime) {
        CocoaFileSystemProxyProvider.runtime = runtime;
    }

    @Override
    public FileSystemReference createTempFile() {
        return runtime.createTempFile();
    }

    @Override
    public FileSystemReference commitTempFile(FileSystemReference sourceFile, long fileId, String fileName) {
        return runtime.commitTempFile(sourceFile, fileId, fileName);
    }

    @Override
    public boolean isFsPersistent() {
        return runtime.isFsPersistent();
    }

    @Override
    public FileSystemReference fileFromDescriptor(String descriptor) {
        return runtime.fileFromDescriptor(descriptor);
    }
}
