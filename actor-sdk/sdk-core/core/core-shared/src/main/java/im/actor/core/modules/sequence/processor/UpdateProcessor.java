/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.sequence.processor;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiUser;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.api.updates.UpdateMessageRead;
import im.actor.core.api.updates.UpdateMessageReadByMe;
import im.actor.core.api.updates.UpdateMessageReceived;
import im.actor.core.entity.Group;
import im.actor.core.entity.Peer;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.CallsProcessor;
import im.actor.core.modules.contacts.ContactsProcessor;
import im.actor.core.modules.encryption.EncryptedProcessor;
import im.actor.core.modules.eventbus.EventBusProcessor;
import im.actor.core.modules.groups.GroupsProcessor;
import im.actor.core.modules.presence.PresenceProcessor;
import im.actor.core.modules.settings.SettingsProcessor;
import im.actor.core.modules.stickers.StickersProcessor;
import im.actor.core.modules.typing.TypingProcessor;
import im.actor.core.modules.messaging.MessagesProcessor;
import im.actor.core.modules.sequence.internal.CombinedDifference;
import im.actor.core.modules.sequence.internal.GetDiffCombiner;
import im.actor.core.modules.users.UsersProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Function;
import im.actor.runtime.function.Predicate;
import im.actor.runtime.function.Supplier;
import im.actor.runtime.function.Tuple2;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.promise.PromisesArray;

public class UpdateProcessor extends AbsModule {

    // Do Not Remove! WorkAround for missing j2objc translator include
    private static final Void DUMB = null;

    private MessagesProcessor messagesProcessor;

    // Seq Processors
    private final SequenceProcessor[] sequenceProcessors;

    // Weak Processors
    private final TypingProcessor typingProcessor;
    private final WeakProcessor[] weakProcessors;

    public UpdateProcessor(ModuleContext context) {
        super(context);

        this.messagesProcessor = new MessagesProcessor(context);
        sequenceProcessors = new SequenceProcessor[]{
                messagesProcessor,
                new UsersProcessor(context),
                new GroupsProcessor(context),
                new ContactsProcessor(context),
                new EncryptedProcessor(context),
                new StickersProcessor(context),
                new SettingsProcessor(context)
        };

        this.typingProcessor = new TypingProcessor(context);
        this.weakProcessors = new WeakProcessor[]{
                typingProcessor,
                new PresenceProcessor(context),
                new EventBusProcessor(context),
                new CallsProcessor(context)
        };
    }


    //
    // Update Handling
    //

    public void processWeakUpdate(Update update, long date) {
        for (WeakProcessor w : weakProcessors) {
            if (w.process(update, date)) {
                return;
            }
        }
    }

    public Promise<Void> processUpdate(Update update) {

        // Small hack for stopping typing indicator
        if (update instanceof UpdateMessage) {
            UpdateMessage message = (UpdateMessage) update;
            typingProcessor.onMessage(message.getPeer(), message.getSenderUid());
        }

        for (SequenceProcessor sequenceProcessor : sequenceProcessors) {
            Promise<Void> res = sequenceProcessor.process(update);
            if (res != null) {
                return res;
            }
        }

        return Promise.success(null);
    }


    //
    // Difference
    //

    public Promise<Void> applyDifferenceUpdate(List<Update> updates) {

        CombinedDifference combinedDifference = GetDiffCombiner.buildDiff(updates);

        ArrayList<Supplier<Promise<Void>>> pending = new ArrayList<>();

        pending.add(() -> messagesProcessor.onDifferenceStart());

        for (Peer peer : combinedDifference.getReceived().keySet()) {
            long time = combinedDifference.getReceived().get(peer);
            pending.add(() -> processUpdate(new UpdateMessageReceived(buildApiPeer(peer), time, 0)));
        }

        for (Peer peer : combinedDifference.getRead().keySet()) {
            long time = combinedDifference.getRead().get(peer);
            pending.add(() -> processUpdate(new UpdateMessageRead(buildApiPeer(peer), time, 0)));
        }

        for (Peer peer : combinedDifference.getReadByMe().keySet()) {
            CombinedDifference.ReadByMeValue time = combinedDifference.getReadByMe().get(peer);
            pending.add(() -> processUpdate(new UpdateMessageReadByMe(buildApiPeer(peer), time.getDate(), time.getCounter())));
        }

        for (Peer peer : combinedDifference.getMessages().keySet()) {
            pending.add(() -> messagesProcessor.onDifferenceMessages(buildApiPeer(peer), combinedDifference.getMessages().get(peer)));
        }

        for (Update u : combinedDifference.getOtherUpdates()) {
            pending.add(() -> processUpdate(u));
        }

        pending.add(() -> messagesProcessor.onDifferenceEnd());

        return Promises.traverse(pending)
                .map(v -> null);
    }
}
