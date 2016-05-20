package im.actor.runtime.android;

import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import im.actor.runtime.AssetsRuntime;

public class AndroidAssetsProvider implements AssetsRuntime {

    @Override
    public boolean hasAsset(String name) {
//        AssetManager assets = AndroidContext.getContext().getResources().getAssets();
//        if (assets.)
        return false;
    }

    @Override
    public String loadAsset(String name) {
        AssetManager assets = AndroidContext.getContext().getResources().getAssets();
        InputStream stream = null;
        try {
            stream = assets.open(name);
            return readFully(stream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public byte[] loadBinAsset(String name) {
        AssetManager assets = AndroidContext.getContext().getResources().getAssets();
        InputStream stream = null;
        try {
            stream = assets.open(name);
            return readFully(stream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String readFully(InputStream inputStream, String encoding) throws IOException {
        return new String(readFully(inputStream), encoding);
    }

    private byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toByteArray();
    }
}
