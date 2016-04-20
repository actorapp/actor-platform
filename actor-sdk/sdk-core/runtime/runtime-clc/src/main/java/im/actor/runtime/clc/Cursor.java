package im.actor.runtime.clc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by mohammad on 11/18/15.
 */
public class Cursor {
    private static final Logger logger = LoggerFactory.getLogger(Cursor.class);

    private ResultSet resultSet;

    public Cursor() {
    }

    public Cursor(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    /**
     * We can't use this method in sqlite jdbc driver
     * @return
     */
    public boolean moveToFirst() {
        try {
            return resultSet.first();
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }

    public Long getLong(int i) {
        try {
            return resultSet.getLong(i);
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return 0L;
    }

    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
    }

    public boolean moveToNext() {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }

    public int getInt(int i) {
        try {
            return resultSet.getInt(i);
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return 0;
    }

    public String getString(int queryColumn) {
        try {
            resultSet.getString(queryColumn);
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }


    public long getLong(String col) {
        try {
            return resultSet.getLong(col);
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return 0;
    }

    public String getString(String col) {
        try {
            return resultSet.getString(col);
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return col;
    }

    public byte[] getBlob(String col) {
        try {
            return resultSet.getBytes(col);
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public byte[] getBlob(int i) {
        try {
            return resultSet.getBytes(1);
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }
}
