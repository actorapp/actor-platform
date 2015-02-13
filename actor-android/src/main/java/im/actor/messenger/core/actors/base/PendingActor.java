package im.actor.messenger.core.actors.base;

import android.database.sqlite.SQLiteDatabase;
import com.droidkit.actors.Actor;
import com.droidkit.engine.persistence.PersistenceSet;
import com.droidkit.engine.persistence.SerializableMap;
import com.droidkit.engine.persistence.storage.SqliteStorage;

import java.io.Serializable;

/**
 * Created by ex3ndr on 07.10.14.
 */
public abstract class PendingActor<T extends Serializable> extends Actor {

    private final String name;
    private final SQLiteDatabase sqLiteDatabase;

    private PersistenceSet<T> pending;

    public PendingActor(String name, SQLiteDatabase sqLiteDatabase) {
        this.name = name;
        this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override
    public void preStart() {
        super.preStart();
        pending = new PersistenceSet<T>(new SerializableMap<T>(new SqliteStorage(sqLiteDatabase, "pending_" + name)));

        for (T t : pending) {
            self().send(new PerformAction<T>(t));
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof PerformAction) {
            performAction(((PerformAction<T>) message).action);
        }
    }

    protected void addAction(T action) {
        if (pending.add(action)) {
            self().send(new PerformAction<T>(action));
        }
    }

    protected abstract void performAction(T action);

    protected void onActionCompleted(T ob) {
        pending.remove(ob);
    }

    private class PerformAction<T> {
        private T action;

        private PerformAction(T action) {
            this.action = action;
        }

        public T getAction() {
            return action;
        }
    }
}
