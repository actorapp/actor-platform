package im.actor.core.modules.sequence.processor;

import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public interface SequenceProcessor {
    Promise<Void> process(Update update);
}
