package im.actor.runtime.files;

import java.util.HashMap;

public final class FileTypes {

    public static final int TYPE_UNKNOWN = -1;

    // Docs
    public static final int TYPE_DOCUMENT = 1;
    public static final int TYPE_SPREAD_SHEET = 2;
    public static final int TYPE_PRESENTATION = 13;

    // Media
    public static final int TYPE_MUSIC = 0;
    public static final int TYPE_BOOK = 3;
    public static final int TYPE_PICTURE = 4;
    public static final int TYPE_VIDEO = 5;

    // Binary
    public static final int TYPE_EXECUTABLE = 6;
    public static final int TYPE_ARCHIVE = 7;

    // Hacker files
    public static final int TYPE_HACKER = 12;

    private static final HashMap<String, Integer> TYPES = new HashMap<String, Integer>();

    static {

        // Documents
        TYPES.put("doc", TYPE_DOCUMENT);
        TYPES.put("docm", TYPE_DOCUMENT);
        TYPES.put("docx", TYPE_DOCUMENT);
        TYPES.put("dot", TYPE_DOCUMENT);
        TYPES.put("dotx", TYPE_DOCUMENT);
        TYPES.put("txt", TYPE_DOCUMENT);
        TYPES.put("xlc", TYPE_DOCUMENT);
        TYPES.put("tex", TYPE_DOCUMENT);
        TYPES.put("stw", TYPE_DOCUMENT);
        TYPES.put("sxw", TYPE_DOCUMENT);
        TYPES.put("xlc", TYPE_DOCUMENT);
        TYPES.put("odf", TYPE_DOCUMENT);
        TYPES.put("odt", TYPE_DOCUMENT);
        TYPES.put("ott", TYPE_DOCUMENT);
        TYPES.put("rtf", TYPE_DOCUMENT);
        TYPES.put("pages", TYPE_DOCUMENT);
        TYPES.put("pdf", TYPE_DOCUMENT);

        // SpreadSheets
        TYPES.put("xls", TYPE_SPREAD_SHEET);
        TYPES.put("xlsx", TYPE_SPREAD_SHEET);
        TYPES.put("xlsm", TYPE_SPREAD_SHEET);
        TYPES.put("xlsb", TYPE_SPREAD_SHEET);
        TYPES.put("numbers", TYPE_SPREAD_SHEET);

        // Presentation
        TYPES.put("ppt", TYPE_PRESENTATION);
        TYPES.put("key", TYPE_PRESENTATION);
        TYPES.put("keynote", TYPE_PRESENTATION);

        // Pictures
        TYPES.put("jpg", TYPE_PICTURE);
        TYPES.put("jpeg", TYPE_PICTURE);
        TYPES.put("jp2", TYPE_PICTURE);
        TYPES.put("jps", TYPE_PICTURE);
        TYPES.put("gif", TYPE_PICTURE);
        TYPES.put("tiff", TYPE_PICTURE);
        TYPES.put("png", TYPE_PICTURE);
        TYPES.put("psd", TYPE_PICTURE);
        TYPES.put("sketch", TYPE_PICTURE);
        TYPES.put("webp", TYPE_PICTURE);
        TYPES.put("ico", TYPE_PICTURE);
        TYPES.put("pcx", TYPE_PICTURE);
        TYPES.put("tga", TYPE_PICTURE);
        TYPES.put("raw", TYPE_PICTURE);
        TYPES.put("svg", TYPE_PICTURE);

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

        // Video
        TYPES.put("mp4", TYPE_VIDEO);
        TYPES.put("3gp", TYPE_VIDEO);
        TYPES.put("m4v", TYPE_VIDEO);
        TYPES.put("webm", TYPE_VIDEO);
        TYPES.put("mkv", TYPE_VIDEO);

        // Ebook
        TYPES.put("epub", TYPE_BOOK);
        TYPES.put("fb2", TYPE_BOOK);

        // Executable
        TYPES.put("apk", TYPE_EXECUTABLE);
        TYPES.put("ipa", TYPE_EXECUTABLE);
        TYPES.put("exe", TYPE_EXECUTABLE);
        TYPES.put("dll", TYPE_EXECUTABLE);

        // Archives
        TYPES.put("rar", TYPE_ARCHIVE);
        TYPES.put("zip", TYPE_ARCHIVE);
        TYPES.put("7z", TYPE_ARCHIVE);

        // Hacker docs
        TYPES.put("sh", TYPE_HACKER);
        TYPES.put("xml", TYPE_HACKER);
        TYPES.put("yaml", TYPE_HACKER);
        TYPES.put("json", TYPE_HACKER);
        TYPES.put("md", TYPE_HACKER);
        TYPES.put("info", TYPE_HACKER);
        TYPES.put("ini", TYPE_HACKER);
    }

    public static int getType(String extension) {
        Integer res = TYPES.get(extension.toLowerCase());
        if (res != null) {
            return res;
        }

        return TYPE_UNKNOWN;
    }

    private FileTypes() {
        throw new RuntimeException("Cant create instance");
    }
}
