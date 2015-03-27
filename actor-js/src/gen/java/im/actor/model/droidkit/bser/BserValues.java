package im.actor.model.droidkit.bser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.bser.util.SparseArray;

import static im.actor.model.droidkit.bser.Utils.convertInt;
import static im.actor.model.droidkit.bser.Utils.convertString;

/**
 * Created by ex3ndr on 17.10.14.
 */
public class BserValues {

    private SparseArray<Object> fields;

    public BserValues(SparseArray<Object> fields) {
        this.fields = fields;
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
            Object obj = fields.get(id);
            if (obj instanceof Long) {
                return (Long) obj;
            }
            throw new IncorrectTypeException("Expected type: long, got " + obj.getClass().getSimpleName());
        }
        return defValue;
    }


    public int optInt(int id) throws IOException {
        return convertInt(optLong(id));
    }

    public int getInt(int id) throws IOException {
        return convertInt(getLong(id));
    }

    public int getInt(int id, int defValue) throws IOException {
        return convertInt(getLong(id, defValue));
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

    public byte[] optBytes(int id) throws IOException {
        return getBytes(id, null);
    }

    public byte[] getBytes(int id) throws IOException {
        if (!fields.containsKey(id)) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return getBytes(id, null);
    }

    public byte[] getBytes(int id, byte[] defValue) throws IOException {
        if (fields.containsKey(id)) {
            Object obj = fields.get(id);
            if (obj instanceof byte[]) {
                return (byte[]) obj;
            }
            throw new IncorrectTypeException("Expected type: byte[], got " + obj.getClass().getSimpleName());
        }
        return defValue;
    }


    public String optString(int id) throws IOException {
        return convertString(optBytes(id));
    }

    public String getString(int id) throws IOException {
        return convertString(getBytes(id));
    }

    public String getString(int id, String defValue) throws IOException {
        return convertString(getBytes(id, defValue.getBytes("UTF-8")));
    }


    public <T extends BserObject> T optObj(int id, T obj) throws IOException {
        byte[] data = optBytes(id);
        if (data == null) {
            return null;
        }
        return Bser.parse(obj, data);
    }

    public <T extends BserObject> T getObj(int id, T obj) throws IOException {
        byte[] data = optBytes(id);
        if (data == null) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return Bser.parse(obj, new DataInput(data, 0, data.length));
    }


    // Repeated values

    public int getRepeatedCount(int id) throws IOException {
        if (fields.containsKey(id)) {
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

    public List<Long> getRepeatedLong(int id) throws IOException {
        ArrayList<Long> res = new ArrayList<Long>();
        if (fields.containsKey(id)) {
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

    public List<Integer> getRepeatedInt(int id) throws IOException {
        List<Long> src = getRepeatedLong(id);
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (Long l : src) {
            res.add(convertInt(l));
        }
        return res;
    }


    // Repeated bytes values

    public List<byte[]> getRepeatedBytes(int id) throws IOException {
        ArrayList<byte[]> res = new ArrayList<byte[]>();
        if (fields.containsKey(id)) {
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

    public List<String> getRepeatedString(int id) throws IOException {
        List<byte[]> src = getRepeatedBytes(id);
        ArrayList<String> res = new ArrayList<String>();
        for (byte[] l : src) {
            res.add(convertString(l));
        }
        return res;
    }


    // Deprecated

    @Deprecated
    public <T extends BserObject> List<T> getRepeatedObj(int id, List<T> objs) throws IOException {
        ArrayList<T> res = new ArrayList<T>();
        for (byte[] v : getRepeatedBytes(id)) {
            res.add(Bser.parse(objs.remove(0), new DataInput(v, 0, v.length)));
        }
        return res;
    }
}