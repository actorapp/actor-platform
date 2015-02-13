package im.actor.crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

/**
 * Created by ex3ndr on 12.10.14.
 */
public class FileCipher {
    private static final int AES_KEY_SIZE = 32;
    private static final int MAC_KEY_SIZE = 32;

    private final Cipher cipher = Crypto.createAESCipher();
    private final Mac mac = Crypto.createHmacSHA256();
    private final byte[] cipherKey;
    private final byte[] macKey;

    private final SecretKeySpec cipherKeySpec;
    private final SecretKeySpec macKeySpec;

    public FileCipher() {
        cipherKey = Crypto.generateSeed(AES_KEY_SIZE);
        macKey = Crypto.generateSeed(MAC_KEY_SIZE);
        cipherKeySpec = new SecretKeySpec(cipherKey, "AES");
        macKeySpec = new SecretKeySpec(macKey, "HmacSHA256");
    }

    public FileCipher(byte[] key) {
        if (key.length != AES_KEY_SIZE + MAC_KEY_SIZE) {
            throw new IllegalArgumentException("key.length != " + (AES_KEY_SIZE + MAC_KEY_SIZE));
        }
        byte[][] sKey = Utils.split(key, AES_KEY_SIZE, MAC_KEY_SIZE);
        cipherKey = sKey[0];
        macKey = sKey[1];
        cipherKeySpec = new SecretKeySpec(cipherKey, "AES");
        macKeySpec = new SecretKeySpec(macKey, "HmacSHA256");
    }

    public byte[] getKey() {
        return Utils.concat(cipherKey, macKey);
    }

    public synchronized byte[] encrypt(byte[] plainText) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, cipherKeySpec);
            mac.init(macKeySpec);

            byte[] ciphertext = cipher.doFinal(plainText);
            byte[] iv = cipher.getIV();

            mac.update(iv);
            byte[] macValue = mac.doFinal(ciphertext);

            return Utils.concat(iv, ciphertext, macValue);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized byte[] decrypt(byte[] cipherText) {
        try {
            if (cipherText.length <= cipher.getBlockSize() + mac.getMacLength()) {
                throw new IllegalArgumentException("Message is too short");
            }

            mac.init(macKeySpec);

            byte[][] ciphertextParts = Utils.split(cipherText, cipher.getBlockSize(),
                    cipherText.length - this.cipher.getBlockSize() - this.mac.getMacLength(),
                    this.mac.getMacLength());

            this.mac.update(cipherText, 0, cipherText.length - mac.getMacLength());
            byte[] ourMac = this.mac.doFinal();

            if (!Utils.equals(ourMac, ciphertextParts[2])) {
                throw new IllegalArgumentException("Mac doesn't match!");
            }

            this.cipher.init(Cipher.DECRYPT_MODE, this.cipherKeySpec,
                    new IvParameterSpec(ciphertextParts[0]));

            return cipher.doFinal(ciphertextParts[1]);
        } catch (InvalidKeyException e) {
            throw new AssertionError(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new AssertionError(e);
        } catch (IllegalBlockSizeException e) {
            throw new AssertionError(e);
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public synchronized void encrypt(String srcFile, String destFile) throws IOException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(destFile);

            cipher.init(Cipher.ENCRYPT_MODE, cipherKeySpec);
            mac.init(macKeySpec);

            byte[] iv = cipher.getIV();
            mac.update(iv);
            outputStream.write(iv);

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

                mac.update(oB, 0, obSize);
                outputStream.write(oB, 0, obSize);
            }

            out = cipher.doFinal();
            mac.update(out);
            outputStream.write(out);

            byte[] macValue = mac.doFinal();

            outputStream.write(macValue);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
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

    public synchronized void decrypt(String srcFile, String destFile) throws IOException {

        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(destFile);

            int fileSize = inputStream.available();
            int contentSize = fileSize - mac.getMacLength() - cipher.getBlockSize();

            byte[] iv = Utils.readBytes(cipher.getBlockSize(), inputStream);

            cipher.init(Cipher.DECRYPT_MODE, cipherKeySpec, new IvParameterSpec(iv));
            mac.init(macKeySpec);
            mac.update(iv);

            byte[] buffer = new byte[1024];
            byte[] out = new byte[cipher.getOutputSize(1024)];
            int count;
            int readSize = 0;

            while ((count = inputStream.read(buffer, 0, Math.min(contentSize - readSize, buffer.length))) > 0) {
                readSize += count;

                mac.update(buffer, 0, count);

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

            byte[] ourMac = mac.doFinal();
            byte[] msgMac = Utils.readBytes(mac.getMacLength(), inputStream);

            if (!Utils.equals(ourMac, msgMac)) {
                throw new RuntimeException("Incorrect MAC");
            }
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
