package im.actor.runtime;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.mvvm.BaseValueModel;
import im.actor.runtime.mvvm.PlatformDisplayList;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.mvvm.ValueModelCreator;
import im.actor.runtime.storage.IndexStorage;
import im.actor.runtime.storage.KeyValueItem;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.PreferencesStorage;

public class Storage {

    private static final StorageRuntime storageRuntime = new StorageRuntimeProvider();
    private static final EnginesRuntime enginesRuntime = new EnginesRuntimeProvider();
    private static final FileSystemRuntime fileSystemRuntime = new FileSystemRuntimeProvider();

    // Runtimes

    public static StorageRuntime getStorageRuntime() {
        return storageRuntime;
    }

    public static EnginesRuntime getEnginesRuntime() {
        return enginesRuntime;
    }

    public static FileSystemRuntime getFileSystemRuntime() {
        return fileSystemRuntime;
    }


    // Storage

    public static PreferencesStorage createPreferencesStorage() {
        return storageRuntime.createPreferencesStorage();
    }

    public static IndexStorage createIndex(String name) {
        return storageRuntime.createIndex(name);
    }

    public static KeyValueStorage createKeyValue(String name) {
        return storageRuntime.createKeyValue(name);
    }

    public static <T extends BserObject & ListEngineItem> ListEngine<T> createList(String name, BserCreator<T> creator) {
        return enginesRuntime.createListEngine(storageRuntime.createList(name), creator);
    }

    public static <V extends BaseValueModel<T>,
            T extends BserObject & KeyValueItem> MVVMCollection<T, V> createKeyValue(String name,
                                                                                     ValueModelCreator<T, V> wrapperCreator,
                                                                                     BserCreator<T> creator) {
        return new MVVMCollection<T, V>(storageRuntime.createKeyValue(name), wrapperCreator, creator);
    }

    public static <T extends BserObject & ListEngineItem> PlatformDisplayList<T> createDisplayList(ListEngine<T> engine,
                                                                                           boolean isSharedInstance,
                                                                                           String entityName) {
        return enginesRuntime.createDisplayList(engine, isSharedInstance, entityName);
    }

    public static void resetStorage() {
        storageRuntime.resetStorage();
    }

    // Files

    public static FileSystemReference createTempFile() {
        return fileSystemRuntime.createTempFile();
    }

    public static boolean isFsPersistent() {
        return fileSystemRuntime.isFsPersistent();
    }

    public static FileSystemReference fileFromDescriptor(String descriptor) {
        return fileSystemRuntime.fileFromDescriptor(descriptor);
    }

    public static FileSystemReference commitTempFile(FileSystemReference sourceFile, long fileId, String fileName) {
        return fileSystemRuntime.commitTempFile(sourceFile, fileId, fileName);
    }
}
