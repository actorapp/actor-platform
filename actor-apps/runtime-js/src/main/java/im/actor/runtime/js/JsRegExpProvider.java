package im.actor.runtime.js;

import im.actor.runtime.RegExpRuntime;
import im.actor.runtime.js.regexp.JsPattern;
import im.actor.runtime.regexp.PatternCompat;

public class JsRegExpProvider implements RegExpRuntime {
    @Override
    public PatternCompat getPattern(String pattern) {
        return new JsPattern(pattern);
    }
}