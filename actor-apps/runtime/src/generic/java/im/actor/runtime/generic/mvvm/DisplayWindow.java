/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.mvvm;

class DisplayWindow {

    private boolean isForwardLoading = false;
    private boolean isBackwardLoading = false;

    private boolean isInited = false;

    private boolean isBackwardLoaded = false;
    private Long currentBackwardHead;

    private boolean isForwardCompleted = false;
    private Long currentForwardHead;

    public synchronized Long getCurrentBackwardHead() {
        return currentBackwardHead;
    }

    public synchronized Long getCurrentForwardHead() {
        return currentForwardHead;
    }

    public synchronized boolean isInited() {
        return isInited;
    }

    public boolean isForwardCompleted() {
        return isForwardCompleted;
    }

    public boolean isBackwardLoaded() {
        return isBackwardLoaded;
    }

    public synchronized boolean startForwardLoading() {
        if (!isInited) {
            return false;
        }
        if (isForwardCompleted) {
            return false;
        }
        if (isForwardLoading) {
            return false;
        }
        isForwardLoading = true;
        return true;
    }

    public synchronized void completeForwardLoading() {
        isForwardLoading = false;
    }

    public synchronized void onForwardCompleted() {
        isForwardCompleted = true;
        currentForwardHead = null;
    }

    public synchronized void onForwardSliceLoaded(Long tail) {
        currentForwardHead = tail;
    }

    public synchronized boolean startBackwardLoading() {
        if (!isInited) {
            return false;
        }
        if (isBackwardLoaded) {
            return false;
        }
        if (isBackwardLoading) {
            return false;
        }
        isBackwardLoading = true;
        return true;
    }

    public synchronized void endBackwardLoading() {
        isBackwardLoading = false;
    }

    public synchronized void onBackwardCompleted() {
        isBackwardLoaded = true;
        currentBackwardHead = null;
    }

    public synchronized void onBackwardSliceLoaded(Long head) {
        currentBackwardHead = head;
    }

    public synchronized void startInitCenter() {
        isInited = false;

        isForwardCompleted = false;
        isForwardLoading = false;
        isBackwardLoading = false;
        isBackwardLoaded = false;
        currentBackwardHead = null;
        currentForwardHead = null;
    }

    public synchronized void completeInitCenter(Long forwardHead, Long backwardHead) {
        isInited = true;
        currentForwardHead = forwardHead;
        currentBackwardHead = backwardHead;
    }

    public synchronized void emptyInit() {
        isInited = true;
        currentBackwardHead = null;
        currentForwardHead = null;
        isForwardCompleted = true;
        isForwardLoading = false;
        isBackwardLoaded = true;
        isBackwardLoading = false;
    }

    public synchronized void startInitForward() {
        isInited = false;

        isForwardCompleted = false;
        isForwardLoading = false;
        isBackwardLoading = false;
        isBackwardLoaded = true;
        currentBackwardHead = null;
        currentForwardHead = null;
    }

    public synchronized void completeInitForward(Long tail) {
        isInited = true;
        currentForwardHead = tail;
    }

    public synchronized void startInitBackward() {
        isInited = false;

        isForwardCompleted = true;
        isForwardLoading = false;
        isBackwardLoading = false;
        isBackwardLoaded = false;
        currentBackwardHead = null;
        currentForwardHead = null;
    }

    public synchronized void completeInitBackward(Long head) {
        isInited = true;
        currentBackwardHead = head;
    }
}
