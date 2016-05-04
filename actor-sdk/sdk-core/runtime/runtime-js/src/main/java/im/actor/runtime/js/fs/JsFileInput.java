/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.fs;

import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.files.FilePart;
import im.actor.runtime.files.InputFile;
import im.actor.runtime.promise.Promise;

public class JsFileInput implements InputFile {

    private JsBlob jsFile;

    public JsFileInput(JsBlob jsFile) {
        this.jsFile = jsFile;
    }

    @Override
    public Promise<FilePart> read(int fileOffset, int len) {
        return new Promise<>(resolver -> {
            JsFileReader fileReader = JsFileReader.create();
            fileReader.setOnLoaded(message -> {
                Uint8Array array = TypedArrays.createUint8Array(message);
                byte[] data = new byte[len];
                for (int i = 0; i < len; i++) {
                    data[i] = (byte) (array.get(i));
                }
                resolver.result(new FilePart(fileOffset, len, data));
            });
            fileReader.readAsArrayBuffer(jsFile.slice(fileOffset, fileOffset + len));
        });
    }

    @Override
    public Promise<Void> close() {
        return Promise.success(null);
    }
}
