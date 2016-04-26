package im.actor.core.modules.sequence.processor;

import im.actor.core.network.parser.Update;

public interface SequenceProcessor {
    boolean process(Update update);
}
