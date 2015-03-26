package im.actor.images.loading.log;

import com.droidkit.actors.Actor;

/**
 * Created by ex3ndr on 20.08.14.
 */
public class LogActor extends Actor {
    @Override
    public void onReceive(Object message) {
        if (sender() != null) {
            android.util.Log.d("ImageKit", sender().getPath() + ":" + message);
        } else {
            android.util.Log.d("ImageKit", "" + message);
        }
    }
}
