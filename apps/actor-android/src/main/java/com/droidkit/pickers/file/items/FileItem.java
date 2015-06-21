package com.droidkit.pickers.file.items;

import android.content.Context;

import com.droidkit.pickers.file.util.TimeUtils;

import java.io.File;

import im.actor.messenger.R;

/**
 * Created by kiolt_000 on 15/09/2014.
 */
public class FileItem extends ExplorerItem {


    public FileItem(String path) {
        super(path);
    }

    public FileItem(File file) {
        super(file);
    }

    public FileItem(File file, boolean selected) {
        super(file, selected);
    }

    public FileItem(File file, boolean selected, String fileType) {
        super(file, selected, fileType);
    }


    public FileItem(File file, boolean selected, String fileType, int imageId) {
        super(file, selected, fileType, imageId, true);
    }

    @Override
    public String getSubtitle(Context context) {
        String convertedSize = null;
        long size = (int) file.length();
        if (size > 1024 * 1024 * 1024) {
            convertedSize = (size / (1024 * 1024 * 1024)) + "." + ((size % (1024 * 1024 * 1024)) / (100 * 1024 * 1024)) + " " + context.getString(R.string.picker_gbytes);
        }
        if (size > 1024 * 1024) {
            convertedSize = (size / (1024 * 1024)) + "." + ((size % (1024 * 1024)) / (100 * 1024)) + " " + context.getString(R.string.picker_mbytes);
        }
        if (convertedSize == null) {
            if (size / 1024 == 0) {
                convertedSize = context.getString(R.string.picker_bytes, size);
            } else
                convertedSize = (size / (1024)) + " " + context.getString(R.string.picker_kbytes);
        }

        long date = file.lastModified();
        String subtitle = convertedSize;
        if (date != 0) {
            subtitle += ", " + TimeUtils.formatFileTime(date, context);
        }
        return subtitle;
    }


}