package im.actor.messenger.util.io;

import android.content.Context;

import com.droidkit.bser.Bser;
import com.droidkit.bser.BserObject;

import java.io.IOException;

/**
 * Created by ex3ndr on 01.12.14.
 */
public class BserPersistence<T extends BserObject> {
    private SafeFileWriter fileWriter;
    private Class<T> clazz;
    private T obj;

    public BserPersistence(Context context, String fileName, Class<T> clazz) {
        this.fileWriter = new SafeFileWriter(context, fileName);
        this.clazz = clazz;

        byte[] data = fileWriter.loadData();
        if (data != null && data.length > 0) {
            try {
                obj = Bser.parse(clazz, data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
        if (obj == null) {
            fileWriter.saveData(new byte[0]);
        } else {
            fileWriter.saveData(obj.toByteArray());
        }
    }
}
