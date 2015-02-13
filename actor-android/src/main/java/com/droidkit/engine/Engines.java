package com.droidkit.engine;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.android.UiActorDispatcher;
import com.droidkit.actors.mailbox.ActorDispatcher;

/**
 * Engine library initialization.
 */
public class Engines {

    private static volatile boolean isInited = false;
    private static Object initLock = new Object();

    /**
     * Pefrorm initialization\
     */
    public static void init() {
        if (!isInited) {
            synchronized (initLock) {
                if (!isInited) {
                    isInited = true;
                    ActorSystem.system().addDispatcher("db", new ActorDispatcher("db", ActorSystem.system(), 1, Thread.MIN_PRIORITY));
                    ActorSystem.system().addDispatcher("ui", new UiActorDispatcher("ui", ActorSystem.system()));
                }
            }
        }
    }
}
