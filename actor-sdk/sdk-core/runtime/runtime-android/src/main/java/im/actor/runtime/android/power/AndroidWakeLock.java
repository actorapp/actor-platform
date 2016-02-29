package im.actor.runtime.android.power;

import android.content.Context;
import android.os.PowerManager;

import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.power.WakeLock;

public class AndroidWakeLock implements WakeLock {

    private static PowerManager.WakeLock wakeLock;

    static {
        PowerManager powerManager = (PowerManager) AndroidContext.getContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ActorWakelock");
    }

    public AndroidWakeLock() {
        acquire();
    }

    @Override
    public void releaseLock() {
        wakeLock.release();
    }

    private static void acquire(){
        if(!wakeLock.isHeld()){
            wakeLock.acquire();
        }
    }
}
