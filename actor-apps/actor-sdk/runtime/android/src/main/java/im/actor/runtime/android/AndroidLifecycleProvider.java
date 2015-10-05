/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */
package im.actor.runtime.android;

import im.actor.runtime.LifecycleRuntime;

public class AndroidLifecycleProvider implements LifecycleRuntime {

    @Override
    public void killApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
