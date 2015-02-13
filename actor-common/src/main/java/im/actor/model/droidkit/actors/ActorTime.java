package im.actor.model.droidkit.actors;

import im.actor.model.droidkit.actors.conf.EnvConfig;

/**
 * Time used by actor system, uses System.nanoTime() inside
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public class ActorTime {
    /**
     * Getting current actor system time
     *
     * @return actor system time
     */
    public static long currentTime() {
        return EnvConfig.getJavaFactory().getCurrentTime();
    }
}