package im.actor.model.mvvm;

/**
* Created by ex3ndr on 14.03.15.
*/
class DisplayWindow {

    private boolean isTailLoading = false;
    private boolean isHeadLoading = false;

    private boolean isInited = false;

    private boolean isHeadLoaded = false;
    private Long currentHead;

    private boolean isTailLoaded = false;
    private Long currentTail;

    public synchronized Object getCurrentHead() {
        return currentHead;
    }

    public synchronized Object getCurrentTail() {
        return currentTail;
    }

    public synchronized boolean isInited() {
        return isInited;
    }

    public boolean isTailLoaded() {
        return isTailLoaded;
    }

    public boolean isHeadLoaded() {
        return isHeadLoaded;
    }

    public synchronized boolean startTailLoading() {
        if (!isInited) {
            return false;
        }
        if (isTailLoaded) {
            return false;
        }
        if (isTailLoading) {
            return false;
        }
        isTailLoading = true;
        return true;
    }

    public synchronized void endTileLoading() {
        isTailLoading = false;
    }

    public synchronized void onTailCompleted() {
        isTailLoaded = true;
        currentTail = null;
    }

    public synchronized void onTailSliceLoaded(Long tail) {
        currentTail = tail;
    }

    public synchronized boolean startHeadLoading() {
        if (!isInited) {
            return false;
        }
        if (isHeadLoaded) {
            return false;
        }
        if (isHeadLoading) {
            return false;
        }
        isHeadLoading = true;
        return true;
    }

    public synchronized void endHeadLoading() {
        isHeadLoading = false;
    }

    public synchronized void onHeadCompleted() {
        isHeadLoaded = true;
        currentHead = null;
    }

    public synchronized void onHeadSliceLoaded(Long head) {
        currentHead = head;
    }

    public synchronized void startInitCenter() {
        isInited = false;

        isTailLoaded = false;
        isTailLoading = false;
        isHeadLoading = false;
        isHeadLoaded = false;
        currentHead = null;
        currentTail = null;
    }

    public synchronized void stopInitCenter(Long tail, Long head) {
        isInited = true;
        currentTail = tail;
        currentHead = head;
    }

    public synchronized void startInitForward() {
        isInited = false;

        isTailLoaded = false;
        isTailLoading = false;
        isHeadLoading = false;
        isHeadLoaded = true;
        currentHead = null;
        currentTail = null;
    }

    public synchronized void stopInitForward(Long tail) {
        isInited = true;
        currentTail = tail;
    }

    public synchronized void startInitBackward() {
        isInited = false;

        isTailLoaded = true;
        isTailLoading = false;
        isHeadLoading = false;
        isHeadLoaded = false;
        currentHead = null;
        currentTail = null;
    }

    public synchronized void stopInitBackward(Long head) {
        isInited = true;
        currentHead = head;
    }
}
