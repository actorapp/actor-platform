/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import java.util.HashMap;

import im.actor.runtime.Crypto;
import im.actor.runtime.FileSystemRuntime;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.js.fs.JsBlob;
import im.actor.runtime.js.fs.JsFileSystemReference;

public class JsFileSystemProvider implements FileSystemRuntime {

    private HashMap<String, JsBlob> files = new HashMap<String, JsBlob>();

    public String registerUploadFile(JsBlob file) {
        String res = "file://" + Crypto.hex(Crypto.randomBytes(16));
        files.put(res, file);
        return res;
    }

    public String registerMemoryFile(byte[] content) {
        String res = "memory://" + Crypto.hex(Crypto.randomBytes(16));
        files.put(res, JsBlob.createBlob(content));
        return res;
    }

    @Override
    public FileSystemReference createTempFile() {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public FileSystemReference commitTempFile(FileSystemReference sourceFile, long fileId, String fileName) {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public boolean isFsPersistent() {
        return false;
    }

    @Override
    public FileSystemReference fileFromDescriptor(String descriptor) {
        if (files.containsKey(descriptor)) {
            return new JsFileSystemReference(files.get(descriptor), descriptor);
        }
        return null;
    }
}
