package com.droidkit.pickers.file.util;

import java.util.HashMap;

/**
 * Created by kiolt_000 on 14/09/2014.
 */
public class FileTypes {

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_MUSIC = -1;
    public static final int TYPE_PICTURE = -2;
    public static final int TYPE_DOC = -3;
    public static final int TYPE_RAR = -4;
    public static final int TYPE_VIDEO = -5;
    public static final int TYPE_APK = -6;
    public static final int TYPE_ZIP = -7;
    public static final int TYPE_XLS = -8;
    public static final int TYPE_PPT = -9;
    public static final int TYPE_CSV = -10;
    public static final int TYPE_HTM = -11;
    public static final int TYPE_HTML = -12;
    public static final int TYPE_PDF = -13;

    private static final HashMap<String, Integer> TYPES = new HashMap<String, Integer>();

    static {
        // Music types
        TYPES.put("mp3", TYPE_MUSIC);
        TYPES.put("m4a", TYPE_MUSIC);
        TYPES.put("ogg", TYPE_MUSIC);
        TYPES.put("flac", TYPE_MUSIC);
        TYPES.put("alac", TYPE_MUSIC);
        TYPES.put("m3u", TYPE_MUSIC);
        TYPES.put("wav", TYPE_MUSIC);
        TYPES.put("wma", TYPE_MUSIC);
        TYPES.put("aac", TYPE_MUSIC);

        // Documents
        TYPES.put("doc", TYPE_DOC);
        TYPES.put("docx", TYPE_DOC);
        TYPES.put("txt", TYPE_DOC);
        TYPES.put("xlc", TYPE_DOC);

        // Pictures
        TYPES.put("jpg", TYPE_PICTURE);
        TYPES.put("jpeg", TYPE_PICTURE);
        TYPES.put("gif", TYPE_PICTURE);
        TYPES.put("tiff", TYPE_PICTURE);
        TYPES.put("png", TYPE_PICTURE);
        TYPES.put("psd", TYPE_PICTURE);

        // Video
        TYPES.put("mp4", TYPE_VIDEO);
        TYPES.put("3gp", TYPE_VIDEO);
        TYPES.put("m4v", TYPE_VIDEO);

        // Other
        TYPES.put("pdf", TYPE_PDF);
        TYPES.put("apk", TYPE_APK);
        TYPES.put("rar", TYPE_RAR);
        TYPES.put("zip", TYPE_ZIP);
        TYPES.put("xls", TYPE_XLS);
        TYPES.put("ppt", TYPE_PPT);
        TYPES.put("csv", TYPE_CSV);
        TYPES.put("htm", TYPE_HTM);
        TYPES.put("html", TYPE_HTML);
    }

    public static int getType(String extension) {
        Integer res = TYPES.get(extension.toLowerCase());
        if (res != null) {
            return res;
        }

        return TYPE_UNKNOWN;
    }
}