package im.actor.core.modules.sequence.processor;

import im.actor.core.network.parser.Update;

public interface WeakProcessor {
    boolean process(Update update, long date);
}
