package im.actor.keygen;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws DigestException, NoSuchAlgorithmException, IOException {
        SecureRandom secureRandom = new SecureRandom();
        if (!new File("keys").exists()) {
            new File("keys").mkdir();
        }
        for (int i = 0; i < 4; i++) {
            File pubFile = new File("keys/actor-key-" + i + ".pub");
            File keyFile = new File("keys/actor-key-" + i + ".key");
            if (pubFile.exists() && keyFile.exists()) {
                System.out.println("Key #" + i + " exists. Skipping...");
                continue;
            }
            Curve25519KeyPair keyPair = Curve25519.keyGen(secureRandom.generateSeed(64));
            FileUtils.writeByteArrayToFile(pubFile, keyPair.getPublicKey());
            FileUtils.writeByteArrayToFile(keyFile, keyPair.getPrivateKey());
        }
        System.out.println("Shared Secret: " + Base64.getEncoder().encodeToString(secureRandom.generateSeed(64)));
    }
}