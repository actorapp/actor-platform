package im.actor.runtime.clc;

import im.actor.runtime.LogRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by amir on 3/12/16.
 */
public class ClcLogProvider implements LogRuntime {
    private static final Logger logger = LoggerFactory.getLogger(ClcLogProvider.class);

    @Override
    public void w(String tag, String message) {
        logger.warn(tag + ":" + message);
    }

    @Override
    public void e(String tag, Throwable throwable) {
        logger.error(tag,throwable);
    }

    @Override
    public void d(String tag, String message) {
        logger.debug(tag + ":" + message);
    }

    @Override
    public void v(String tag, String message) {
        logger.warn(tag + ":" + message);
    }
}
