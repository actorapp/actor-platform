package im.actor.runtime.generic;

import im.actor.runtime.RegExpRuntime;
import im.actor.runtime.generic.regexp.GenericPattern;
import im.actor.runtime.regexp.PatternCompat;

public class GenericRegExpProvider implements RegExpRuntime {

    @Override
    public PatternCompat getPattern(String pattern) {
        return new GenericPattern(pattern);
    }
}
