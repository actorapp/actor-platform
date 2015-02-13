package im.actor.messenger.util.io;

import java.io.*;
import java.lang.reflect.Field;

public abstract class PersistenceObject implements Serializable {

    protected abstract OutputStream openWrite(String path) throws FileNotFoundException;

    protected abstract InputStream openRead(String path, boolean error) throws IOException;

    protected InputStream openRead(String path) throws IOException {
        return openRead(path, false);
    }

    public boolean trySave() {
        try {
            save();
            return true;
        } catch (IOException e) {
            // Log.e("PersistenceObject", e);
            return false;
        } catch (ClassNotFoundException e) {
            // Log.e("PersistenceObject", e);
            return false;
        } catch (Exception e) {
            // Log.e("PersistenceObject", e);
            return false;
        }
    }

    public boolean tryLoad() {
        try {
            load();
            return true;
        } catch (IOException e) {
            // Log.e("PersistenceObject", e);
            return false;
        } catch (ClassNotFoundException e) {
            // Log.e("PersistenceObject", e);
            return false;
        } catch (Exception e) {
            // Log.e("PersistenceObject", e);
            return false;
        }
    }

    public void save() throws IOException, ClassNotFoundException {
        beforeSave();
        ObjectOutputStream out = new ObjectOutputStream(openWrite(getClass().getName() + ".sav"));
        out.writeObject(this);
        out.close();
        afterSave();
    }

    protected void beforeSave() {

    }

    protected void afterSave() {

    }

    private void load(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(is);
        Object data = in.readObject();
        if (data.getClass().equals(getClass())) {
            for (Field field : getClass().getDeclaredFields()) {
                field.setAccessible(true);

                try {
                    Class<?> type = field.getType();
                    if (type == Integer.TYPE) {
                        field.setInt(this, field.getInt(data));
                    } else if (type == Byte.TYPE) {
                        field.setByte(this, field.getByte(data));
                    } else if (type == Character.TYPE) {
                        field.setChar(this, field.getChar(data));
                    } else if (type == Short.TYPE) {
                        field.setShort(this, field.getShort(data));
                    } else if (type == Boolean.TYPE) {
                        field.setBoolean(this, field.getBoolean(data));
                    } else if (type == Long.TYPE) {
                        field.setLong(this, field.getLong(data));
                    } else if (type == Float.TYPE) {
                        field.setFloat(this, field.getFloat(data));
                    } else if (type == Double.TYPE) {
                        field.setDouble(this, field.getDouble(data));
                    } else {
                        field.set(this, field.get(data));
                    }
                } catch (IllegalAccessException e) {
                    // Log.e("PersistenceObject", e);
                }
            }
        }
        in.close();
    }

    public void load() throws IOException, ClassNotFoundException {
        beforeLoad();
        try {
            load(openRead(getClass().getName() + ".sav", false));
        } catch (Exception e) {
            load(openRead(getClass().getName() + ".sav", true));
        }
        afterLoad();
    }

    protected void beforeLoad() {

    }

    protected void afterLoad() {

    }

}