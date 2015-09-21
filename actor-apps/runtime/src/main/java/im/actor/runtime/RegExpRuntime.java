/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;
import im.actor.runtime.regexp.PatternCompat;

/**
 * Provider for reg exp matching.
 */
public interface RegExpRuntime {
    /**
     * Creates a new PatternCompat instance
     *
     * @param pattern
     * @return
     */
    @ObjectiveCName("getPatternWithPattern:")
    public abstract PatternCompat getPattern(String pattern);

    /**
     * Creates a new PatternCompat instance
     *
     * @param pattern
     * @param flags
     * @return
     */
    @ObjectiveCName("getPatternWithExp:withFlags:")
    public abstract PatternCompat getPattern(String pattern, String flags);
}