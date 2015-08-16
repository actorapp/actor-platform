/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors.dispatch;

import im.actor.runtime.threading.ThreadLocalCompat;

public class DispatchResult {

    private static ThreadLocalCompat<DispatchResult> FREE_RESULTS = im.actor.runtime.Runtime.createThreadLocal();

    public static DispatchResult result(Object res) {
        DispatchResult result = FREE_RESULTS.get();
        if (result != null) {
            FREE_RESULTS.remove();
            result.update(true, res, 0);
        } else {
            result = new DispatchResult(true, res, 0);
        }
        return result;
    }

    public static DispatchResult delay(long delay) {
        DispatchResult result = FREE_RESULTS.get();
        if (result != null) {
            FREE_RESULTS.remove();
            result.update(false, null, delay);
        } else {
            result = new DispatchResult(false, null, delay);
        }
        return result;
    }


    private boolean isResult;
    private Object res;
    private long delay;

    DispatchResult(boolean isResult, Object res, long delay) {
        this.isResult = isResult;
        this.res = res;
        this.delay = delay;
    }

    void update(boolean isResult, Object res, long delay) {
        this.isResult = isResult;
        this.res = res;
        this.delay = delay;
    }

    public boolean isResult() {
        return isResult;
    }

    public Object getRes() {
        return res;
    }

    public long getDelay() {
        return delay;
    }

    public void recycle() {
        FREE_RESULTS.set(this);
    }
}
