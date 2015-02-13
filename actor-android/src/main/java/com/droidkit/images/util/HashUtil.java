package com.droidkit.images.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ex3ndr on 17.08.14.
 */
public class HashUtil {
    public static String md5(String s) {
        try {
            byte[] bytesOfMessage = s.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(bytesOfMessage);
            return hex(thedigest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static String md5(byte[] s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(s);
            return hex(thedigest);
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static String hex(byte[] data) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            res.append(Integer.toHexString(data[i] & 0xFF).toLowerCase());
        }
        return res.toString();
    }

}
