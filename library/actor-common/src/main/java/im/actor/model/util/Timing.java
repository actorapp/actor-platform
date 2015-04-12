package im.actor.model.util;

import im.actor.model.droidkit.actors.ActorTime;
import im.actor.model.log.Log;

/**
 * Created by ex3ndr on 09.04.15.
 */
public class Timing {
    private String sectionName;
    private long sectionStart;
    private final String TAG;

    public Timing(String tag) {
        this.TAG = tag;
    }

    public void section(String sectionName) {
        end();
        this.sectionName = sectionName;
        this.sectionStart = ActorTime.currentTime();
    }

    public void end() {
        if (this.sectionName != null) {
            Log.d(TAG, "" + this.sectionName + " loaded in " + (sectionStart - ActorTime.currentTime()) + " ms");
            this.sectionName = null;
        }
    }
}
