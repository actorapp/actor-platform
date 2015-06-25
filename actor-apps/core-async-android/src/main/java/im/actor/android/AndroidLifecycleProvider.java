/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */
package im.actor.android;

import im.actor.model.LifecycleProvider;

public class AndroidLifecycleProvider implements LifecycleProvider {

    @Override
    public void killApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
