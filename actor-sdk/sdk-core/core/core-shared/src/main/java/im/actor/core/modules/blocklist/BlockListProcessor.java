package im.actor.core.modules.blocklist;

import im.actor.core.api.updates.UpdateUserBlocked;
import im.actor.core.api.updates.UpdateUserUnblocked;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;

public class BlockListProcessor extends AbsModule implements SequenceProcessor {

    public BlockListProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public boolean process(Update update) {
        if (update instanceof UpdateUserBlocked) {
            UpdateUserBlocked blocked = (UpdateUserBlocked) update;
            context().getBlockList().markBlocked(blocked.getUid());
            return true;
        } else if (update instanceof UpdateUserUnblocked) {
            UpdateUserUnblocked unblocked = (UpdateUserUnblocked) update;
            context().getBlockList().markNonBlocked(unblocked.getUid());
            return true;
        }
        return false;
    }
}
