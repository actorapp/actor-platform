package im.actor.runtime.android.power;

import android.content.Context;
import android.os.PowerManager;

import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.power.WakeLock;

public class AndroidWakeLock implements WakeLock {


    private final PowerManager.WakeLock wakeLock;

    public AndroidWakeLock() {
        PowerManager powerManager = (PowerManager) AndroidContext.getContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ActorWakelock");
        wakeLock.acquire();
    }

    @Override
    public void releaseLock() {
        if (wakeLock!=null && wakeLock.isHeld()){
            wakeLock.release();
        }
    }
}
