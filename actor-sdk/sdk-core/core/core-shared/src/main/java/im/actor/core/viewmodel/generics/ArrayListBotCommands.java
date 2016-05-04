package im.actor.core.viewmodel.generics;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.core.entity.BotCommand;
import im.actor.core.entity.ContactRecord;

public class ArrayListBotCommands extends ArrayList<BotCommand> {

    public ArrayListBotCommands(int capacity) {
        super(capacity);
    }

    public ArrayListBotCommands() {
    }

    public ArrayListBotCommands(Collection<? extends BotCommand> collection) {
        super(collection);
    }

    @Override
    public BotCommand get(int index) {
        return super.get(index);
    }
}
