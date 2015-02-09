package com.droidkit.bser;

import com.droidkit.bser.util.SparseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 17.10.14.
 */
public class BserValues {
    private SparseArray<Object> fields;

    public BserValues(SparseArray<Object> fields) {
        this.fields = fields;
    }

    public int optInt(int id) {
        return getInt(id, 0);
    }

    public int getInt(int id) {
        if (!fields.containsKey(id)) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return getInt(id, 0);
    }

    public int getInt(int id, int defValue) {
        if (fields.containsKey(id)) {
            Object obj = fields.get(id);
            if (obj instanceof Byte) {
                return (Byte) obj;
            } else if (obj instanceof Integer) {
                return (Integer) obj;
            } else if (obj instanceof Long) {
                return (int) (long) (Long) obj;
            } else if (obj instanceof String) {
                String s = (String) obj;
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            throw new IncorrectTypeException("Expected type: int, got " + obj.getClass().getSimpleName());
        }
        return defValue;
    }


    public long optLong(int id) {
        return getLong(id, 0);
    }

    public long getLong(int id) {
        if (!fields.containsKey(id)) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return getLong(id, 0);
    }

    public long getLong(int id, long defValue) {
        if (fields.containsKey(id)) {
            Object obj = fields.get(id);
            if (obj instanceof Byte) {
                return (Byte) obj;
            } else if (obj instanceof Integer) {
                return (Integer) obj;
            } else if (obj instanceof Long) {
                return (Long) obj;
            } else if (obj instanceof String) {
                String s = (String) obj;
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            throw new IncorrectTypeException("Expected type: long, got " + obj.getClass().getSimpleName());
        }
        return defValue;
    }


    public String optString(int id) {
        return getString(id, null);
    }

    public String getString(int id) {
        if (!fields.containsKey(id)) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return getString(id, null);
    }

    public String getString(int id, String defValue) {
        if (fields.containsKey(id)) {
            Object res = fields.get(id);
            if (res instanceof byte[]) {
                return new String((byte[]) res);
            }
            return res + "";
        }
        return defValue;
    }


    public byte[] optBytes(int id) {
        return getBytes(id, null);
    }

    public byte[] getBytes(int id) {
        if (!fields.containsKey(id)) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return getBytes(id, null);
    }

    public byte[] getBytes(int id, byte[] defValue) {
        if (fields.containsKey(id)) {
            Object obj = fields.get(id);
            if (obj instanceof byte[]) {
                return (byte[]) obj;
            }
            throw new IncorrectTypeException("Expected type: byte[], got " + obj.getClass().getSimpleName());
        }
        return defValue;
    }


    public double optDouble(int id) {
        return getDouble(id, 0);
    }

    public double getDouble(int id) {
        if (!fields.containsKey(id)) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return getDouble(id, 0);
    }

    public double getDouble(int id, double defValue) {
        long res = getLong(id, Double.doubleToLongBits(defValue));
        return Double.longBitsToDouble(res);
    }


    public boolean optBool(int id) {
        return getBool(id, false);
    }

    public boolean getBool(int id) {
        if (!fields.containsKey(id)) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return getBool(id, false);
    }

    public boolean getBool(int id, boolean defValue) {
        long res = getLong(id, defValue ? 1 : 0);
        return res != 0;
    }


    public <T extends BserObject> T getObj(int id, Class<T> clazz) throws IOException {
        byte[] data = getBytes(id);
        if (data == null) {
            throw new UnknownFieldException("Unable to find field #" + id);
        }
        return Bser.parse(clazz, data);
    }

    public <T extends BserObject> T optObj(int id, Class<T> clazz) throws IOException {
        byte[] data = optBytes(id);
        if (data == null) {
            return null;
        }
        return Bser.parse(clazz, data);
    }


    public <T extends BserComposite> T getComposite(BserCompositeFieldDescription<T> fieldDescritption) throws IOException {
        T res = fieldDescritption.readObject(this);
        if (res == null) {
            throw new UnknownFieldException("Unable to find field for composite type");
        }
        return res;
    }

    public <T extends BserComposite> T optComposite(BserCompositeFieldDescription<T> fieldDescritption) throws IOException {
        return fieldDescritption.readObject(this);
    }

    public List<Integer> getRepeatedInt(int id) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        if (fields.containsKey(id)) {
            Object val = fields.get(id);
            if (val instanceof Long) {
                res.add((int) (long) (Long) val);
            } else if (val instanceof Integer) {
                res.add((Integer) val);
            } else if (val instanceof List) {
                List<Object> rep = (List) val;

                for (Object val2 : rep) {
                    if (val2 instanceof Long) {
                        res.add((int) (long) (Long) val2);
                    } else if (val2 instanceof Integer) {
                        res.add((Integer) val2);
                    } else {
                        throw new IncorrectTypeException("Expected type: int, got " + val2.getClass().getSimpleName());
                    }
                }
            } else {
                throw new IncorrectTypeException("Expected type: int, got " + val.getClass().getSimpleName());
            }
        }
        return res;
    }

    public List<Long> getRepeatedLong(int id) {
        ArrayList<Long> res = new ArrayList<Long>();
        if (fields.containsKey(id)) {
            Object val = fields.get(id);
            if (val instanceof Long) {
                res.add((Long) val);
            } else if (val instanceof Integer) {
                res.add((long) (int) (Integer) val);
            } else if (val instanceof List) {
                List<Object> rep = (List) val;

                for (Object val2 : rep) {
                    if (val2 instanceof Long) {
                        res.add((Long) val2);
                    } else if (val2 instanceof Integer) {
                        res.add((long) (int) (Integer) val2);
                    } else {
                        throw new IncorrectTypeException("Expected type: long, got " + val2.getClass().getSimpleName());
                    }
                }
            } else {
                throw new IncorrectTypeException("Expected type: long, got " + val.getClass().getSimpleName());
            }
        }
        return res;
    }

    public List<String> getRepeatedString(int id) {
        ArrayList<String> res = new ArrayList<String>();
        if (fields.containsKey(id)) {
            Object val = fields.get(id);
            if (val instanceof Long) {
                res.add("" + val);
            } else if (val instanceof Integer) {
                res.add("" + val);
            } else if (val instanceof byte[]) {
                res.add(new String((byte[]) val));
            } else if (val instanceof List) {
                List<Object> rep = (List) val;

                for (Object val2 : rep) {
                    if (val2 instanceof Long) {
                        res.add("" + val2);
                    } else if (val2 instanceof Integer) {
                        res.add("" + val2);
                    } else if (val2 instanceof byte[]) {
                        res.add(new String((byte[]) val2));
                    } else {
                        throw new IncorrectTypeException("Expected type: byte[], got " + val2.getClass().getSimpleName());
                    }
                }
            } else {
                throw new IncorrectTypeException("Expected type: byte[], got " + val.getClass().getSimpleName());
            }
        }
        return res;
    }

    public List<byte[]> getRepeatedBytes(int id) {
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
                        throw new IncorrectTypeException("Expected type: byte[], got " + val2.getClass().getSimpleName());
                    }
                }
            } else {
                throw new IncorrectTypeException("Expected type: byte[], got " + val.getClass().getSimpleName());
            }
        }
        return res;
    }

    public <T extends BserObject> List<T> getRepeatedObj(int id, Class<T> clazz) throws IOException {
        ArrayList<T> res = new ArrayList<T>();
        if (fields.containsKey(id)) {
            Object val = fields.get(id);
            if (val instanceof byte[]) {
                res.add(Bser.parse(clazz, (byte[]) val));
            } else if (val instanceof List) {
                List<Object> rep = (List) val;

                for (Object val2 : rep) {
                    if (val2 instanceof byte[]) {
                        res.add(Bser.parse(clazz, (byte[]) val2));
                    } else {
                        throw new IncorrectTypeException("Expected type: byte[], got " + val2.getClass().getSimpleName());
                    }
                }
            } else {
                throw new IncorrectTypeException("Expected type: byte[], got " + val.getClass().getSimpleName());
            }
        }
        return res;
    }
}