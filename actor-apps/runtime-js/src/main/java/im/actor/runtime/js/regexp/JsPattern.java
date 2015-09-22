package im.actor.runtime.js.regexp;

import com.google.gwt.regexp.shared.RegExp;
import im.actor.runtime.regexp.MatcherCompat;
import im.actor.runtime.regexp.PatternCompat;

public class JsPattern extends PatternCompat {
    private RegExp compiled;

    public JsPattern(String pattern) {
        super(pattern);
        this.compiled = RegExp.compile(pattern);
    }

    @Override
    public MatcherCompat matcher(String input) {
        return new JsMatcher(compiled.exec(input));
    }
}