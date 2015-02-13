package im.actor.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

/**
 * Created by ex3ndr on 13.10.14.
 */
public class ObsoleteFileCipher {
    private final Cipher cipher = Crypto.createAESCipher();
    private final byte[] cipherAesKey;
    private final byte[] cipherAesIv;

    public ObsoleteFileCipher(byte[] key) {
        byte[][] sKey = Utils.split(key, 32, 16);
        this.cipherAesKey = sKey[0];
        this.cipherAesIv = sKey[1];
    }

    public synchronized void decrypt(String srcFile, String destFile) throws IOException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(destFile);

            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cipherAesKey, "AES"),
                    new IvParameterSpec(cipherAesIv));

            byte[] buffer = new byte[1024];
            byte[] out = new byte[cipher.getOutputSize(1024)];
            int count;

            while ((count = inputStream.read(buffer)) > 0) {
                byte[] oB;
                int obSize;

                try {
                    obSize = cipher.update(buffer, 0, count, out, 0);
                    oB = out;
                } catch (ShortBufferException e) {
                    e.printStackTrace();
                    oB = cipher.update(buffer, 0, count);
                    obSize = oB.length;
                }


                outputStream.write(oB, 0, obSize);
            }

            out = cipher.doFinal();
            outputStream.write(out);

        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {

                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
