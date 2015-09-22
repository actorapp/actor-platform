package im.actor.runtime;

import im.actor.runtime.regexp.PatternCompat;

public class RegExpRuntimeProvider implements RegExpRuntime {

    @Override
    public PatternCompat getPattern(String exp) {
        throw new RuntimeException("Dumb");
    }
}