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
     * @param pattern pattern for matching
     * @return return compiled pattern
     */
    @ObjectiveCName("getPatternWithPattern:")
    PatternCompat getPattern(String pattern);
}