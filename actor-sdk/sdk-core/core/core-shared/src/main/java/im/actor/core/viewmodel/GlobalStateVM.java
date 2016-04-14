package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.core.events.AppVisibleChanged;
import im.actor.core.events.ConnectingStateChanged;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.core.viewmodel.generics.IntValueModel;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

public class GlobalStateVM {

    @Property("nonatomic, readonly")
    private BooleanValueModel isAppVisible;
    @Property("nonatomic, readonly")
    private BooleanValueModel isConnecting;
    @Property("nonatomic, readonly")
    private BooleanValueModel isSyncing;
    @Property("nonatomic, readonly")
    private IntValueModel globalCounter;
    @Property("nonatomic, readonly")
    private IntValueModel globalTempCounter;

    public GlobalStateVM(ModuleContext context) {
        this.isAppVisible = new BooleanValueModel("app.visible", false);
        this.globalCounter = new IntValueModel("app.counter", null);
        this.globalTempCounter = new IntValueModel("app.temp_counter", 0);
        this.isConnecting = new BooleanValueModel("app.connecting", false);
        this.isSyncing = new BooleanValueModel("app.syncing", false);

        context.getEvents().subscribe(new BusSubscriber() {
            @Override
            public void onBusEvent(Event event) {
                if (event instanceof AppVisibleChanged) {
                    if (((AppVisibleChanged) event).isVisible()) {
                        isAppVisible.change(true);
                        globalTempCounter.change(0);
                    } else {
                        isAppVisible.change(false);
                    }
                }
            }
        }, AppVisibleChanged.EVENT);

        context.getEvents().subscribe(new BusSubscriber() {
            @Override
            public void onBusEvent(Event event) {
                isConnecting.change(((ConnectingStateChanged) event).isConnecting());
            }
        }, ConnectingStateChanged.EVENT);
    }


    /**
     * Notify from Modules about global counters changed
     *
     * @param value current value of global counter
     */
    public synchronized void onGlobalCounterChanged(int value) {
        globalCounter.change(value);
        if (!isAppVisible.get()) {
            globalTempCounter.change(value);
        }
    }

    /**
     * Is syncing in progress
     *
     * @return View Model of Boolean
     */
    public BooleanValueModel getIsSyncing() {
        return isSyncing;
    }

    /**
     * Is Connecting in progress
     *
     * @return View Model of Boolean
     */
    public BooleanValueModel getIsConnecting() {
        return isConnecting;
    }


    /**
     * Gettting global unread counter
     *
     * @return View Model of Integer
     */
    public IntValueModel getGlobalCounter() {
        return globalCounter;
    }

    /**
     * Getting global unread counter that resets when app is opened
     *
     * @return View Model of Integer
     */
    public IntValueModel getGlobalTempCounter() {
        return globalTempCounter;
    }

    /**
     * Is App visible state
     *
     * @return View Model of Boolean
     */
    public BooleanValueModel getIsAppVisible() {
        return isAppVisible;
    }
}
