/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.bser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.collections.SparseArray;

public class BserValues {

    private SparseArray<Object> fields;

    // TODO: Replace with SparseBooleanArray
    private SparseArray<Boolean> touched = new SparseArray<Boolean>();

    public BserValues(@NotNull SparseArray<Object> fields) {
        this.fields = fields;
    }

    public boolean hasRemaining() {
        for (int i = 0; i < fields.size(); i++) {
            if (!touched.get(fields.keyAt(i), false)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public SparseArray<Object> buildRemaining() {
        SparseArray<Object> res = new SparseArray<Object>();
        for (int i = 0; i < fields.size(); i++) {
            int key = fields.keyAt(i);
            if (!touched.get(key, false)) {
                res.put(key, fields.get(key));
            }
        }
        return res;
    }

    // Long based values

    public long optLong(int id) throws IOException {
        return getLong(id, 0);
    }

    public long getLong(int id) throws IOException {
        if (!fields.containsKey(id)) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return getLong(id, 0);
    }

    public long getLong(int id, long defValue) throws IOException {
        if (fields.containsKey(id)) {
            touched.put(id, true);
            Object obj = fields.get(id);
            if (obj instanceof Long) {
                return (Long) obj;
            }
            throw new IncorrectTypeException("Expected type: long, got " + obj.getClass().getSimpleName());
        }
        return defValue;
    }


    public int optInt(int id) throws IOException {
        return Utils.convertInt(optLong(id));
    }

    public int getInt(int id) throws IOException {
        return Utils.convertInt(getLong(id));
    }

    public int getInt(int id, int defValue) throws IOException {
        return Utils.convertInt(getLong(id, defValue));
    }


    public double optDouble(int id) throws IOException {
        return Double.longBitsToDouble(optLong(id));
    }

    public double getDouble(int id) throws IOException {
        return Double.longBitsToDouble(getLong(id));
    }

    public double getDouble(int id, double defValue) throws IOException {
        return Double.longBitsToDouble(getLong(id, Double.doubleToLongBits(defValue)));
    }


    public boolean optBool(int id) throws IOException {
        return optLong(id) != 0;
    }

    public boolean getBool(int id) throws IOException {
        return getLong(id) != 0;
    }

    public boolean getBool(int id, boolean defValue) throws IOException {
        return getLong(id, defValue ? 1 : 0) != 0;
    }


    // Bytes based values

    @Nullable
    public byte[] optBytes(int id) throws IOException {
        return getBytes(id, null);
    }

    @Nullable
    public byte[] getBytes(int id) throws IOException {
        if (!fields.containsKey(id)) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return getBytes(id, null);
    }

    @NotNull
    public byte[] getBytes(int id, @NotNull byte[] defValue) throws IOException {
        // TODO: Check defValue == null?
        if (fields.containsKey(id)) {
            touched.put(id, true);
            Object obj = fields.get(id);
            if (obj instanceof byte[]) {
                return (byte[]) obj;
            }
            throw new IncorrectTypeException("Expected type: byte[], got " + obj.getClass().getSimpleName());
        }
        return defValue;
    }


    @Nullable
    public String optString(int id) throws IOException {
        return Utils.convertString(optBytes(id));
    }

    @NotNull
    public String getString(int id) throws IOException {
        return Utils.convertString(getBytes(id));
    }

    @NotNull
    public String getString(int id, @NotNull String defValue) throws IOException {
        return Utils.convertString(getBytes(id, defValue.getBytes("UTF-8")));
    }


    @Nullable
    public <T extends BserObject> T optObj(int id, @NotNull T obj) throws IOException {
        byte[] data = optBytes(id);
        if (data == null) {
            return null;
        }
        return Bser.parse(obj, data);
    }

    @NotNull
    public <T extends BserObject> T getObj(int id, @NotNull T obj) throws IOException {
        byte[] data = optBytes(id);
        if (data == null) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return Bser.parse(obj, new DataInput(data, 0, data.length));
    }


    // Repeated values

    public int getRepeatedCount(int id) throws IOException {
        if (fields.containsKey(id)) {
            touched.put(id, true);
            Object val = fields.get(id);
            if (val instanceof List) {
                return ((List) val).size();
            } else {
                return 1;
            }
        }
        return 0;
    }


    // Repeated long values

    @NotNull
    public List<Long> getRepeatedLong(int id) throws IOException {
        ArrayList<Long> res = new ArrayList<Long>();
        if (fields.containsKey(id)) {
            touched.put(id, true);
            Object val = fields.get(id);
            if (val instanceof Long) {
                res.add((Long) val);
            } else if (val instanceof List) {
                List<Object> rep = (List) val;
                for (Object val2 : rep) {
                    if (val2 instanceof Long) {
                        res.add((Long) val2);
                    } else {
                        throw new IOException("Expected type: long, got " + val2.getClass().getSimpleName());
                    }
                }
            } else {
                throw new IOException("Expected type: long, got " + val.getClass().getSimpleName());
            }
        }
        return res;
    }

    @NotNull
    public List<Integer> getRepeatedInt(int id) throws IOException {
        List<Long> src = getRepeatedLong(id);
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (Long l : src) {
            res.add(Utils.convertInt(l));
        }
        return res;
    }


    // Repeated bytes values

    @NotNull
    public List<byte[]> getRepeatedBytes(int id) throws IOException {
        ArrayList<byte[]> res = new ArrayList<byte[]>();
        if (fields.containsKey(id)) {
            touched.put(id, true);
            Object val = fields.get(id);
            if (val instanceof byte[]) {
                res.add((byte[]) val);
            } else if (val instanceof List) {
                List<Object> rep = (List) val;

                for (Object val2 : rep) {
                    if (val2 instanceof byte[]) {
                        res.add((byte[]) val2);
                    } else {
                        throw new IOException("Expected type: byte[], got " + val2.getClass().getSimpleName());
                    }
                }
            } else {
                throw new IOException("Expected type: byte[], got " + val.getClass().getSimpleName());
            }
        }
        return res;
    }

    @NotNull
    public List<String> getRepeatedString(int id) throws IOException {
        List<byte[]> src = getRepeatedBytes(id);
        ArrayList<String> res = new ArrayList<String>();
        for (byte[] l : src) {
            res.add(Utils.convertString(l));
        }
        return res;
    }


    // Deprecated

    @Deprecated
    @NotNull
    public <T extends BserObject> List<T> getRepeatedObj(int id, @NotNull List<T> objs) throws IOException {
        ArrayList<T> res = new ArrayList<T>();
        for (byte[] v : getRepeatedBytes(id)) {
            res.add(Bser.parse(objs.remove(0), new DataInput(v, 0, v.length)));
        }
        return res;
    }
}