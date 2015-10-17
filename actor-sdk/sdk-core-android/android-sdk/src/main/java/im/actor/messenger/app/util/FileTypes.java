package im.actor.messenger.app.util;

import java.util.HashMap;

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
        TYPES.put("docm", TYPE_DOC);
        TYPES.put("dot", TYPE_DOC);
        TYPES.put("dotx", TYPE_DOC);
        TYPES.put("epub", TYPE_DOC);
        TYPES.put("fb2", TYPE_DOC);
        TYPES.put("xml", TYPE_DOC);
        TYPES.put("info", TYPE_DOC);
        TYPES.put("tex", TYPE_DOC);
        TYPES.put("stw", TYPE_DOC);
        TYPES.put("sxw", TYPE_DOC);
        TYPES.put("txt", TYPE_DOC);
        TYPES.put("xlc", TYPE_DOC);
        TYPES.put("odf", TYPE_DOC);
        TYPES.put("odt", TYPE_DOC);
        TYPES.put("ott", TYPE_DOC);
        TYPES.put("rtf", TYPE_DOC);
        TYPES.put("pages", TYPE_DOC);
        TYPES.put("ini", TYPE_DOC);

        // Spreadsheet
        TYPES.put("xls", TYPE_XLS);
        TYPES.put("xlsx", TYPE_XLS);
        TYPES.put("xlsm", TYPE_XLS);
        TYPES.put("xlsb", TYPE_XLS);
        TYPES.put("numbers", TYPE_XLS);

        // Pictures
        TYPES.put("jpg", TYPE_PICTURE);
        TYPES.put("jpeg", TYPE_PICTURE);
        TYPES.put("jp2", TYPE_PICTURE);
        TYPES.put("jps", TYPE_PICTURE);
        TYPES.put("gif", TYPE_PICTURE);
        TYPES.put("tiff", TYPE_PICTURE);
        TYPES.put("png", TYPE_PICTURE);
        TYPES.put("psd", TYPE_PICTURE);
        TYPES.put("webp", TYPE_PICTURE);
        TYPES.put("ico", TYPE_PICTURE);
        TYPES.put("pcx", TYPE_PICTURE);
        TYPES.put("tga", TYPE_PICTURE);
        TYPES.put("raw", TYPE_PICTURE);
        TYPES.put("svg", TYPE_PICTURE);

        // Video
        TYPES.put("mp4", TYPE_VIDEO);
        TYPES.put("3gp", TYPE_VIDEO);
        TYPES.put("m4v", TYPE_VIDEO);
        TYPES.put("webm", TYPE_VIDEO);

        // Presentation
        TYPES.put("ppt", TYPE_PPT);
        TYPES.put("key", TYPE_PPT);
        TYPES.put("keynote", TYPE_PPT);

        // Other
        TYPES.put("pdf", TYPE_PDF);
        TYPES.put("apk", TYPE_APK);
        TYPES.put("rar", TYPE_RAR);
        TYPES.put("zip", TYPE_ZIP);
        TYPES.put("csv", TYPE_CSV);

        TYPES.put("xhtm", TYPE_HTM);
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