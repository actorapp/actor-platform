/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.util;

import im.actor.runtime.actors.ActorTime;
import im.actor.runtime.Log;

/**
 * Calculation of execution duration
 */
public class Timing {
    private String sectionName;
    private long sectionStart;
    private final String TAG;

    /**
     * Create Timing
     *
     * @param tag tag for result log
     */
    public Timing(String tag) {
        this.TAG = tag;
    }

    /**
     * Mark new section start
     *
     * @param sectionName section name
     */
    public void section(String sectionName) {
        end();
        this.sectionName = sectionName;
        this.sectionStart = ActorTime.currentTime();
    }

    /**
     * Mark section end
     */
    public void end() {
        if (this.sectionName != null) {
            Log.d(TAG, "" + this.sectionName + " loaded in " + (ActorTime.currentTime() - sectionStart) + " ms");
            this.sectionName = null;
        }
    }
}
