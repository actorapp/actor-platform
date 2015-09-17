package im.actor.runtime;

import im.actor.runtime.regexp.PatternCompat;

public class RegExpRuntimeProvider implements RegExpRuntime {
    @Override
    public PatternCompat getPattern(String exp) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public PatternCompat getPattern(String pattern, String flags) {
        throw new RuntimeException("Dumb");
    }
}