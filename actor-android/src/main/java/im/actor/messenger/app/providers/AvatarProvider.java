package im.actor.messenger.app.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.droidkit.images.cache.DiskCache;

import java.io.File;
import java.io.FileNotFoundException;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.app.Core;
import im.actor.messenger.app.images.FileKeys;

/**
 * Created by ex3ndr on 22.01.14.
 */
public class AvatarProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        if (BuildConfig.ENABLE_CHROME) {
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"_data"}, 1);
            matrixCursor.addRow(new Object[]{"image.jpg"});
            return matrixCursor;
        }

        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!Core.messenger().isLoggedIn()) {
            throw new IllegalArgumentException();
        }

        long id = Long.parseLong(uri.getLastPathSegment());

        DiskCache diskCache = Core.core().getImageLoader().getInternalDiskCache();
        String key = FileKeys.avatarKey(id);
        String file = diskCache.lockFile(key);
        if (file == null) {
            throw new IllegalArgumentException();
        }
        return ParcelFileDescriptor.open(new File(file), ParcelFileDescriptor.MODE_READ_ONLY);
    }
}