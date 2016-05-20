package im.actor.runtime.clc;

import im.actor.runtime.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteStatementWrapper {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteStatementWrapper.class);
    PreparedStatement statement;

    public SQLiteStatementWrapper(PreparedStatement statement) {
        this.statement = statement;
    }

    public void bindLong(int i, long key) {
        try {
            statement.setLong(i, key);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void bindString(int i, String key) {
        try {
            statement.setString(i, key);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
    public void bindBlob(int i, byte[] key) {
        try {
            statement.setBytes(i, key);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
    public void bindInteger(int i, int key) {
        try {
            statement.setInt(i, key);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void executeInsert() {
        try {
            statement.execute();
        } catch (SQLException e) {
            logger.error("Can't execute insert", e);
        }
    }

    public void execute() {
        try {
            statement.execute();
        } catch (SQLException e) {
            logger.error("Can't execute statment", e);
        }
    }
}
