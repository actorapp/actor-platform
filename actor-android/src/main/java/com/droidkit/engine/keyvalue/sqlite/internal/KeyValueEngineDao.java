package com.droidkit.engine.keyvalue.sqlite.internal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.droidkit.engine._internal.sqlite.AbstractDao;
import com.droidkit.engine.keyvalue.DataAdapter;
import com.droidkit.engine.keyvalue.sqlite.DaoValue;

import java.util.ArrayList;

public class KeyValueEngineDao<V> extends AbstractDao<V> {

    private final DataAdapter<V> classConnector;

    public KeyValueEngineDao(String keyValueEngineName,
                             SQLiteDatabase db,
                             DataAdapter<V> classConnector) {
        super(keyValueEngineName, db, new KeyValueEngineTableStatements(db, keyValueEngineName), classConnector);
        this.classConnector = classConnector;
    }

    @Override
    protected void createTable() {
        if (!isTableExists()) {
            String constraint = "IF NOT EXISTS ";
            db.execSQL("CREATE TABLE " + constraint + "'" + tableName + "' (" + //
                            "'ID' INTEGER NOT NULL," + // 0: id
                            "'BYTES' BLOB NOT NULL," + // 1: bytes
                            "PRIMARY KEY('ID'));"
            );
        }
    }

    @Override
    protected void bindValues(SQLiteStatement stmt, V entity) {
        stmt.clearBindings();

        Long id = classConnector.getId(entity);
        if (id != null) {
            stmt.bindLong(1, id);
        }
        byte[] bytes = binarySerializator.serialize(entity);
        if (bytes != null) {
            stmt.bindBlob(2, bytes);
        }
    }

    @Override
    protected V readEntity(Cursor cursor) {
        return (V) binarySerializator.deserialize(cursor.isNull(1) ? null : cursor.getBlob(1));
    }

    @Override
    protected void deleteByKeyInsideSynchronized(long id, SQLiteStatement stmt) {
        stmt.clearBindings();
        stmt.bindLong(1, id);
        stmt.execute();
    }

    @Override
    public ArrayList<V> getAll() {
        final String stmt = statements.getAllStatement();
        ArrayList<V> res = new ArrayList<V>();
        ArrayList<DaoValue<V>> daoValues = loadAllAndCloseCursor(db.rawQuery(stmt, null));
        for (DaoValue<V> v : daoValues) {
            res.add(v.getValue());
        }
        return res;
    }

    @Override
    public V getById(long id) {
        final String stmt = statements.getGetByIdStatement();

        return loadSingleAndCloseCursor(db.rawQuery(stmt,
                new String[]{
                        String.valueOf(id)
                }
        ));
    }

    @Override
    protected DaoValue<V> createDaoValue(Cursor cursor, V v) {
        long id = cursor.getLong(0);
        return new DaoValue<V>(0, id, 0, v);
    }
}
