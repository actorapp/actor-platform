/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import java.util.HashMap;

import im.actor.model.FileSystemProvider;
import im.actor.model.crypto.CryptoUtils;
import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;
import im.actor.model.js.providers.fs.JsFile;

/**
 * Created by ex3ndr on 03.05.15.
 */
public class JsFileSystemProvider implements FileSystemProvider {

    private HashMap<String, JsFile> files = new HashMap<String, JsFile>();

    public String registerUploadFile(JsFile file) {
        String res = "upload://" + CryptoUtils.hex(CryptoUtils.randomBytes(16));
        files.put(res, file);
        return res;
    }

    @Override
    public FileSystemReference createTempFile() {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public FileSystemReference commitTempFile(FileSystemReference sourceFile, FileReference fileReference) {
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
