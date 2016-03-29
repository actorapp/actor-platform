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
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;

public class GalleryScannerActor extends Actor {

    public static final String TAG = "GALLERY_SCANNER";
    public static final int CHECK_NEW_DELAY = 1000;
    public static final int SCAN_DELAY = 100;
    Context context;
    Uri uri;
    Cursor cursor;
    int offset = 0;
    int column_index_data, column_index_folder_name, column_index_date;

    ArrayList<String> listOfAllImages = new ArrayList<String>();
    ArrayList<String> newMedia = new ArrayList<>();

    String absolutePathOfImage = null;
    GalleryVM galleryVM;

    private static final int SCAN_COUNT = 9;

    ArrayList<String> loaded = new ArrayList<>();
    private boolean visible = false;
    private boolean scanned = false;
    private String[] projection;

    public GalleryScannerActor(Context context, GalleryVM galleryVM) {
        this.context = context;
        this.galleryVM = galleryVM;
    }

    @Override
    public void preStart() {
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public void initScan() {
        Log.d(TAG, "init scan");
        projection = new String[]{MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED};

        cursor = getQuery();

        if (cursor != null && cursor.getCount() > 0) {
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            column_index_date = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED);
            column_index_folder_name = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);


            Log.d(TAG, "init scan ok - starting scan iterations");
            self().send(new Scan());
        } else {
            Log.d(TAG, "init scan - no media, let's check it in " + CHECK_NEW_DELAY);
            schedule(new InitScan(), CHECK_NEW_DELAY);
        }

    }

    public Cursor getQuery() {
        return context.getContentResolver().query(uri, projection, null,
                null, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");
    }

    private void scan() {

        Log.d(TAG, "scan");
        int i = 0;
        while (offset + i++ < offset + SCAN_COUNT && cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        boolean added = false;
        if (offset % 100 == 0) {
            Log.d(TAG, "scan - update vm, offset - " + offset);
            galleryVM.getGalleryMediaPath().change(new ArrayList<String>(listOfAllImages));
            added = true;
        }
        offset += i;
        if (offset < cursor.getCount()) {
            Log.d(TAG, "scan - " + i + " iterations, offset - " + offset + ", schedule next in " + SCAN_DELAY);
            schedule(new Scan(), SCAN_DELAY);
        } else {
            if (!added) {
                galleryVM.getGalleryMediaPath().change(new ArrayList<String>(listOfAllImages));
            }
            Log.d(TAG, "scanned to end, offset - " + offset + ", schedule check new in " + CHECK_NEW_DELAY);
            scanned = true;
            schedule(new CheckNew(), CHECK_NEW_DELAY);
        }
    }

    private void checkNew() {
        Log.d(TAG, "checkNew");
        cursor = getQuery();
        newMedia.clear();
        while (cursor != null && cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            if (!listOfAllImages.contains(absolutePathOfImage)) {
                Bitmap bitmap = ImageHelper.loadOptimizedHQ(absolutePathOfImage);
                if (bitmap != null) {
                    bitmap.recycle();
                    newMedia.add(absolutePathOfImage);
                    Log.d(TAG, "checkNew - new media");

                } else {
                    Log.d(TAG, "checkNew - new media writing, breaking for wait");

                    break;
                }
            } else {
                Log.d(TAG, "checkNew - found old media, break");

                break;
            }
        }
        if (newMedia.size() > 0) {
            listOfAllImages.addAll(0, newMedia);
            galleryVM.getGalleryMediaPath().change(new ArrayList<String>(listOfAllImages));
            Log.d(TAG, "checkNew - new media add - " + newMedia.size());

        }
        if (visible) {
            Log.d(TAG, "checkNew - visible, schedule next check in " + CHECK_NEW_DELAY);

            schedule(new CheckNew(), CHECK_NEW_DELAY);
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
                } else {
                    self().send(new InitScan());
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
