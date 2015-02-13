package com.droidkit.engine._internal.sqlite;

import android.content.ContentResolver;
import android.database.*;
import android.net.Uri;
import android.os.Bundle;

final public class FastCursor implements Cursor {

    private final CursorWindow window;
    private int position;
    private final int count;

    public FastCursor(CursorWindow window) {
        this.window = window;
        count = window.getNumRows();
    }

    @Override
    public int getCount() {
        return window.getNumRows();
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean move(int offset) {
        return moveToPosition(position + offset);
    }

    @Override
    public boolean moveToPosition(int position) {
        if (position >= 0 && position < count) {
            this.position = position;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean moveToFirst() {
        position = 0;
        return count > 0;
    }

    @Override
    public boolean moveToLast() {
        if (count > 0) {
            position = count - 1;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean moveToNext() {
        if (position < count - 1) {
            position++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean moveToPrevious() {
        if (position > 0) {
            position--;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isFirst() {
        return position == 0;
    }

    @Override
    public boolean isLast() {
        return position == count - 1;
    }

    @Override
    public boolean isBeforeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAfterLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnIndex(String columnName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getColumnName(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getColumnNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBlob(int columnIndex) {
        return window.getBlob(position, columnIndex);
    }

    @Override
    public String getString(int columnIndex) {
        return window.getString(position, columnIndex);
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(int columnIndex) {
        return window.getShort(position, columnIndex);
    }

    @Override
    public int getInt(int columnIndex) {
        return window.getInt(position, columnIndex);
    }

    @Override
    public long getLong(int columnIndex) {
        return window.getLong(position, columnIndex);
    }

    @Override
    public float getFloat(int columnIndex) {
        return window.getFloat(position, columnIndex);
    }

    @Override
    public double getDouble(int columnIndex) {
        return window.getDouble(position, columnIndex);
    }

    @Override
    public boolean isNull(int columnIndex) {
        return window.isNull(position, columnIndex);
    }

    @Override
    public void deactivate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean requery() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getExtras() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle respond(Bundle extras) {
        throw new UnsupportedOperationException();
    }

    /** Since API level 11 */
    public int getType(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    /** Since API level 19 */
    public Uri getNotificationUri() {
        return null;
    }

}

