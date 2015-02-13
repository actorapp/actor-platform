package com.droidkit.engine.event;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import com.droidkit.actors.dispatch.RunnableDispatcher;
import com.droidkit.engine.Engines;
import com.droidkit.engine._internal.util.Utils;
import com.droidkit.engine._internal.util.WeakEqualReference;
import com.droidkit.engine.event.NotificationListener;
import com.droidkit.engine.event.State;
import com.droidkit.engine.event.StateInitValue;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationCenter {

    static {
        Engines.init();
    }

    private static final String KEY_DELIMITER = "-";

    private static final String TAG = "NotificationCenter";

    private static final int NO_ID = Integer.MIN_VALUE;

    private volatile static com.droidkit.engine.event.NotificationCenter instance;

    public static com.droidkit.engine.event.NotificationCenter getInstance() {
        if (instance == null) {
            synchronized (com.droidkit.engine.event.NotificationCenter.class) {
                if (instance == null) {
                    instance = new com.droidkit.engine.event.NotificationCenter();
                }
            }
        }
        return instance;
    }

    @SuppressLint("NewApi")
    private NotificationCenter() {
        backgroundFireLoop = new RunnableDispatcher("notification", 1);

        listeners = Collections.newSetFromMap(new ConcurrentHashMap<WeakEqualReference<OnNotificationListenerContainer>, Boolean>());

        states = new ConcurrentHashMap<Integer, State>();

        statesValues = new ConcurrentHashMap<String, Object[]>();
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Object fireRemoveSyncObject = new Object();

    /**
     * Loop for background event fires
     */
    private final RunnableDispatcher backgroundFireLoop;

    /**
     * Active listeners storage
     */
    private final Set<WeakEqualReference<OnNotificationListenerContainer>> listeners;

    /**
     * States storage
     */
    private final ConcurrentHashMap<Integer, State> states;

    /**
     * States values in-memory storage
     */
    private final ConcurrentHashMap<String, Object[]> statesValues;


/////////////////////////////////////////////////////////////////////////////////////////////////////

    public State registerState(final int eventType, final StateInitValue stateInitValue) {
        final State state = new State(eventType, stateInitValue);
        states.put(eventType, state);
        return state;
    }

    /**
     * Add new listener for specified eventType and eventId
     * You must keep a strong reference to your listener somewhere to prevent GC from removing it from memory
     *
     * @param eventType
     * @param eventId
     * @param notificationListener
     */
    public void addListener(int eventType, long eventId, NotificationListener notificationListener) {
        final State state = states.get(eventType);
        if (state != null) {
            Object[] value = statesValues.get(getKeyForEvent(eventType, eventId));

            if (value == null && state.getStateInitValue() != null) {
                value = state.getStateInitValue().initState(eventType, eventId);
            }

            if (notificationListener != null) {
                notificationListener.onNotification(eventType, eventId, value);
            }
        }

        if (notificationListener != null) {
            listeners.add(new WeakEqualReference<OnNotificationListenerContainer>(
                    new OnNotificationListenerContainer(eventType, eventId, notificationListener, Utils.isUIThread())
            ));
        }
    }

    /**
     * The same addListener(int, int, NotificationListener), but with eventId == NO_ID
     *
     * @param eventType
     * @param notificationListener
     */
    public void addListener(int eventType, NotificationListener notificationListener) {
        addListener(eventType, NO_ID, notificationListener);
    }

    public void removeListener(NotificationListener notificationListener) {

        final Iterator<WeakEqualReference<OnNotificationListenerContainer>> it = listeners.iterator();

        while (it.hasNext()) {
            final WeakEqualReference<OnNotificationListenerContainer> weakListenerContainer = it.next();
            final OnNotificationListenerContainer listenerContainer = weakListenerContainer.get();
            if (listenerContainer == null) {
                it.remove();
            } else if (listenerContainer.listener == notificationListener) {
                synchronized (fireRemoveSyncObject) {
                    it.remove();
                    listenerContainer.setDeleted(true);
                }
                //continue iterate after that, because we can have the same NotificationListener
                //for different eventType and eventId parameters
            }
        }
    }

    public void fireEvent(final int eventType, final long eventId, final Object... args) {
        final Iterator<WeakEqualReference<OnNotificationListenerContainer>> it = listeners.iterator();

        final boolean isUiThread = Utils.isUIThread();

        final State state = states.get(eventType);
        if (state != null) {
            statesValues.put(getKeyForEvent(eventType, eventId), args);
        }
        while (it.hasNext()) {
            final WeakEqualReference<OnNotificationListenerContainer> weakListenerContainer = it.next();
            final OnNotificationListenerContainer listenerContainer = weakListenerContainer.get();

            if (listenerContainer == null) {
                it.remove();
            } else if (listenerContainer.eventType == eventType && listenerContainer.eventId == eventId) {

                synchronized (fireRemoveSyncObject) {
                    if (!listenerContainer.isDeleted()) {
                        if (isUiThread && listenerContainer.wasAddedInUIThread) {
                            listenerContainer.listener.onNotification(eventType, eventId, args);
                            continue;
                        }
                        final Runnable fireEvent = new Runnable() {
                            @Override
                            public void run() {
                                synchronized (fireRemoveSyncObject) {
                                    //double-check here
                                    if (!listenerContainer.isDeleted()) {
                                        listenerContainer.listener.onNotification(eventType, eventId, args);
                                    }
                                }
                            }
                        };
                        if (listenerContainer.wasAddedInUIThread) {
                            handler.post(fireEvent);
                        } else {
                            backgroundFireLoop.postAction(fireEvent);
                        }
                    }
                }
            }
        }
    }

    /**
     * The same as fireEvent(int, int, Object[]), but with args == null
     *
     * @param eventType
     * @param eventId
     */
    public void fireEvent(final int eventType, final int eventId) {
        fireEvent(eventType, eventId, null);
    }

    /**
     * The same as fireEvent(int, int, Object[]), but with eventId == NO_ID
     *
     * @param eventType
     * @param args
     */
    public void fireEvent(final int eventType, final Object[] args) {
        fireEvent(eventType, NO_ID, args);
    }

    /**
     * The same as fireEvent(int, int, Object[]), but with args == null and eventId == NO_ID
     *
     * @param eventType
     */
    public void fireEvent(final int eventType) {
        fireEvent(eventType, NO_ID, null);
    }

    private String getKeyForEvent(final int eventType, final long eventId) {
        return eventType + KEY_DELIMITER + eventId;
    }

    /**
     * Container to store NotificationListener in listener's container
     */
    private class OnNotificationListenerContainer {
        int eventType;
        long eventId;
        NotificationListener listener;
        boolean wasAddedInUIThread;
        volatile boolean deleted = false;

        private OnNotificationListenerContainer(int eventType, long eventId,
                                                NotificationListener listener, boolean wasAddedInUIThread) {
            this.eventType = eventType;
            this.eventId = eventId;
            this.listener = listener;
            this.wasAddedInUIThread = wasAddedInUIThread;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OnNotificationListenerContainer that = (OnNotificationListenerContainer) o;

            if (eventId != that.eventId) return false;
            if (eventType != that.eventType) return false;
            if (!listener.equals(that.listener)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = eventType;
            result = 31 * result + ((Long) eventId).hashCode();
            result = 31 * result + listener.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "OnNotificationListenerContainer{" +
                    "eventType=" + eventType +
                    ", eventId=" + eventId +
                    ", listener=" + listener +
                    '}';
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(final boolean deleted) {
            this.deleted = deleted;
        }
    }
}
