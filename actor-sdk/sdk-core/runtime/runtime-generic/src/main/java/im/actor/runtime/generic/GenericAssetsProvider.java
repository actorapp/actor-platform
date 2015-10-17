package im.actor.runtime.generic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import im.actor.runtime.AssetsRuntime;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class GenericAssetsProvider implements AssetsRuntime {

    @Override
    public boolean hasAsset(String name) {
        return getClass().getClassLoader().getResource(name) != null;
    }

    @Override
    public String loadAsset(String name) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        try {
            return readFully(stream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
