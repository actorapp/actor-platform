package im.actor.core.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

import im.actor.core.utils.ImageHelper;
import im.actor.core.viewmodel.GalleryVM;
import im.actor.runtime.actors.Actor;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class GalleryScannerActor extends Actor {

    Context context;
    Uri uri;
    Cursor cursor;
    int offset = 0;
    int column_index_data, column_index_folder_name, column_index_date;

    ArrayList<String> listOfAllImages = new ArrayList<String>();
    ArrayList<String> newMedia = new ArrayList<>();

    String absolutePathOfImage = null;
    GalleryVM galleryVM;
    Uri lastScan;

    private static final int SCAN_COUNT = 10;

    ArrayList<String> loaded = new ArrayList<>();
    private boolean visible = false;
    private boolean scanned = false;
    private String[] projection;

    public GalleryScannerActor(Context context) {
        this.context = context;
    }

    @Override
    public void preStart() {
        galleryVM = messenger().getGalleryVM();
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        self().send(new InitScan());
    }

    public void initScan() {
        projection = new String[]{MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED};

        cursor = getQuery();

        if (cursor != null && cursor.getCount() > 0) {
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            column_index_date = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED);
            column_index_folder_name = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);


            self().send(new Scan());
        } else {
            schedule(new InitScan(), 1000);
        }

    }

    public Cursor getQuery() {
        return context.getContentResolver().query(uri, projection, null,
                null, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");
    }

    private void scan() {


        int i = 0;
        while (offset + i++ < offset + SCAN_COUNT && cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        galleryVM.getGalleryMediaPath().change(new ArrayList<String>(listOfAllImages));
        offset += i;
        if (offset < cursor.getCount()) {
            self().send(new Scan());
        } else {
            lastScan = MediaStore.getMediaScannerUri();
            scanned = true;
            schedule(new CheckNew(), 1000);
        }
    }

    private void checkNew() {
        cursor = getQuery();
        newMedia.clear();
        while (cursor != null && cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            if (!listOfAllImages.contains(absolutePathOfImage)) {
                Bitmap bitmap = ImageHelper.loadOptimizedHQ(absolutePathOfImage);
                if (bitmap != null) {
                    bitmap.recycle();
                    newMedia.add(absolutePathOfImage);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (newMedia.size() > 0) {
            listOfAllImages.addAll(0, newMedia);
            galleryVM.getGalleryMediaPath().change(new ArrayList<String>(listOfAllImages));
        }
        if (visible) {
            schedule(new CheckNew(), 1000);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Scan) {
            scan();
        } else if (message instanceof InitScan) {
            initScan();
        } else if (message instanceof CheckNew) {
            checkNew();
        } else if (message instanceof Visible) {
            if (((Visible) message).isVisible()) {
                visible = true;
                if (scanned) {
                    self().send(new CheckNew());
                }
            } else {
                visible = false;
            }
        }
    }

    private static class Scan {

    }

    private static class InitScan {

    }

    private static class CheckNew {

    }

    public static class Visible {
        boolean visible;

        public Visible(boolean visible) {
            this.visible = visible;
        }

        public boolean isVisible() {
            return visible;
        }
    }
}
