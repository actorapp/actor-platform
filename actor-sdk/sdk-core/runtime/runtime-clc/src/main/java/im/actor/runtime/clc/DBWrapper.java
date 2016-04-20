package im.actor.runtime.clc;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;


public class DBWrapper {
    private static final Logger logger = LoggerFactory.getLogger(DBWrapper.class);

    private final Connection db;

    public DBWrapper(Connection db) {
        this.db = db;
    }

    public void execSQL(String sql) {
        try {
            db.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return query(table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        String query = "SELECT " + Arrays.stream(columns).collect(Collectors.joining(", ")) + " FROM " + table;
        if (selection != null) {
            query += " WHERE " + selection;
        }
        if (groupBy != null) {
            query += " GROUP BY " + groupBy;
        }

        if (having != null) {
            query += " HAVING " + having;
        }

        if (orderBy != null) {
            query += " ORDER BY " + orderBy;
        }

        if (limit != null) {
            query += " LIMIT " + limit;
        }

        return rawQuery(query, selectionArgs);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        try {
            if (selectionArgs != null) {
                for (String select : selectionArgs) {
                    sql = sql.replaceFirst("\\?", select);
                }
            }

            return new Cursor(db.createStatement().executeQuery(sql));
        } catch (SQLException e) {
            logger.error("Error in execute query", e);
        }
        return null;
    }

    public SQLiteStatementWrapper compileStatement(String sql) {
        try {
            return new SQLiteStatementWrapper(db.prepareStatement(sql));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    public void execSQL(String sql, Object[] args) {
        try {
            SQLiteStatementWrapper statement = new SQLiteStatementWrapper(db.prepareStatement(sql));
            int i = 0;
            for (Object arg : args) {
                i++;
                if (arg == null) {
                    continue;
                } else if (arg instanceof byte[] || arg instanceof Byte[]) {
                    statement.bindBlob(i, (byte[]) arg);
                } else if (arg instanceof Long) {
                    statement.bindLong(i, (Long) arg);
                } else if (arg instanceof String) {
                    statement.bindString(i, (String) arg);
                } else if (arg instanceof Integer) {
                    statement.bindInteger(i, (Integer) arg);
                } else {
                    logger.warn("FATAL: not found : " + arg.getClass().toString());
                }

            }

            statement.execute();
        } catch (SQLException e) {
            logger.error("Error in execute query", e);
        }
    }
}
