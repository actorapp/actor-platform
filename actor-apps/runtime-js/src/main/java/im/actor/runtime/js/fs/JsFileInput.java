/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.fs;

import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

import im.actor.runtime.files.FileReadCallback;
import im.actor.runtime.files.InputFile;

public class JsFileInput implements InputFile {

    private JsBlob jsFile;

    public JsFileInput(JsBlob jsFile) {
        this.jsFile = jsFile;
    }

    @Override
    public void read(final int fileOffset, final byte[] data, final int offset, final int len, final FileReadCallback callback) {
        JsFileReader fileReader = JsFileReader.create();
        fileReader.setOnLoaded(new JsFileLoadedClosure() {
            @Override
            public void onLoaded(ArrayBuffer message) {
                Uint8Array array = TypedArrays.createUint8Array(message);
                for (int i = 0; i < len; i++) {
                    data[offset + i] = (byte) (array.get(i));
                }
                callback.onFileRead(fileOffset, data, offset, len);
            }
        });
        fileReader.readAsArrayBuffer(jsFile.slice(fileOffset, fileOffset + len));
    }

    @Override
    public boolean close() {
        return true;
    }
}
