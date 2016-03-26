package im.actor.core.modules.file;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.promise.Promise;

public class FileUrlInt extends ActorInterface {

    public FileUrlInt(@NotNull ActorRef dest) {
        super(dest);
    }

    public Promise<String> askForUrl(long fileId, long accessHash) {
        return ask(new FileUrlLoader.AskUrl(fileId, accessHash));
    }
}
