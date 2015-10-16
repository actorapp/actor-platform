package com.droidkit.pickers.file.util;

import android.os.Environment;

import com.droidkit.pickers.file.items.ExplorerItem;
import com.droidkit.pickers.file.items.FileItem;
import com.droidkit.pickers.file.items.FolderItem;
import com.droidkit.pickers.file.items.PictureItem;

import java.io.File;

import im.actor.messenger.R;

public class Converter {

    public static FileItem getFileItem(File file, boolean selected) {

        if (!file.exists()) {
            return null;
        }

        String fileName = file.getName();
        String fileType = "";
        String[] fileNameSplit = fileName.split("\\.");

        if (fileNameSplit.length > 1) {
            fileType = fileNameSplit[fileNameSplit.length - 1];
        } else {
            return new FileItem(file, selected, "?", R.drawable.picker_unknown);
        }

        int imageId = 0;// R.drawable.file;
        switch (FileTypes.getType(fileType)) {
            case FileTypes.TYPE_PICTURE:
                return new PictureItem(file, selected, fileType);
            case FileTypes.TYPE_MUSIC:
                imageId = R.drawable.picker_music;
                break;
            case FileTypes.TYPE_DOC:
                imageId = R.drawable.picker_doc;
                break;
            case FileTypes.TYPE_PDF:
                imageId = R.drawable.picker_pdf;
                break;
            case FileTypes.TYPE_RAR:
                imageId = R.drawable.picker_rar;
                break;
            case FileTypes.TYPE_APK:
                imageId = R.drawable.picker_apk;
                break;
            case FileTypes.TYPE_VIDEO:
                imageId = R.drawable.picker_video;
                break;
            case FileTypes.TYPE_CSV:
                imageId = R.drawable.picker_csv;
                break;
            case FileTypes.TYPE_HTM:
                imageId = R.drawable.picker_htm;
                break;
            case FileTypes.TYPE_HTML:
                imageId = R.drawable.picker_html;
                break;
            case FileTypes.TYPE_PPT:
                imageId = R.drawable.picker_ppt;
                break;
            case FileTypes.TYPE_XLS:
                imageId = R.drawable.picker_xls;
                break;
            case FileTypes.TYPE_ZIP:
                imageId = R.drawable.picker_zip;
                break;
        }
        return new FileItem(file, selected, fileType, imageId);
    }

    public static FolderItem getFolderItem(File file) {

        int imageId = R.drawable.picker_folder;

        if (file.list() == null || file.getName().toCharArray()[0] == '.') {
            return null;
            /*if (file.getName().toCharArray()[0] == '.') {
                // imageId = R.drawable.folder_locked;
                return null;
            } else
                return new FolderItem(file, R.drawable.picker_system_folder, true);*/
        } else if (file.list().length == 0) {
            // return new FolderItem(file, R.drawable.picker_folder, true);//  picker_folder_empty
        }

        String folderPath = file.getPath();
        if (folderPath.equals(Environment.getExternalStorageDirectory().getPath())) {
            // return new ExternalStorageItem("External memory");
            // imageId = R.drawable.folder_external;
        } else if (folderPath.contains(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath())) {
            imageId = R.drawable.picker_folder_music;
        } else if (folderPath.contains(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath())) {
            imageId = R.drawable.picker_folder_pictures;
        } else if (folderPath.contains(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath())) {
            imageId = R.drawable.picker_folder_download;
        } else //if (Build.VERSION.SDK_INT >= 19 && folderPath.equals(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath())) {
            //  imageId = R.drawable.folder_docs;
            //} else
            if (folderPath.contains(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath())) {
                imageId = R.drawable.picker_folder_video;
            } else if (folderPath.contains(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath())) {
                imageId = R.drawable.picker_folder_camera;
            } else {

                folderPath = folderPath.toLowerCase();
                if (folderPath.contains("music")) {
                    imageId = R.drawable.picker_folder_music;
                } else if (folderPath.contains("picture") || folderPath.contains("image") || folderPath.contains("photo")) {
                    imageId = R.drawable.picker_folder_pictures;
                } else if (folderPath.contains("download")) {
                    imageId = R.drawable.picker_folder_download;
                } else if (folderPath.contains("doc")) {
                    // todo imageId = R.drawable.folder_docs;
                } else if (folderPath.contains("movie") || folderPath.contains("video")) {
                    imageId = R.drawable.picker_folder_video;
                }


            }

        return new FolderItem(file, imageId);
    }

    public static ExplorerItem getItem(File file, boolean selected) {
        return file.isDirectory() ? getFolderItem(file) : getFileItem(file, selected);
    }
}
