package com.droidkit.actors;

import com.droidkit.actors.conf.EnvConfig;

import java.util.Date;

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