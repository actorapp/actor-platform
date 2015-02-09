package com.droidkit.bser;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ex3ndr on 17.10.14.
 */
public class Bser {
    public static <T extends BserObject> T parse(Class<T> clazz, InputStream inputStream) throws IOException {
        T res;
        try {
            res = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        BserValues reader = new BserValues(BserParser.deserialize(inputStream));
        res.parse(reader);
        return res;
    }

    public static <T extends BserObject> T parse(Class<T> clazz, String fileName) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
            return parse(clazz, inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static <T extends BserObject> T parse(Class<T> clazz, byte[] data) throws IOException {
        return parse(clazz, new ByteArrayInputStream(data));
    }
}
