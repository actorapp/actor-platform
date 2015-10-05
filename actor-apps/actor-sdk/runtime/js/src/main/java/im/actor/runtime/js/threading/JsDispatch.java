/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.threading;

import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.dispatch.Dispatch;
import im.actor.runtime.actors.mailbox.ActorDispatcher;
import im.actor.runtime.actors.mailbox.Envelope;
import im.actor.runtime.actors.mailbox.MailboxesQueue;

public class JsDispatch extends ActorDispatcher {
    public JsDispatch(String name, ActorSystem actorSystem) {
        super(name, actorSystem);

        initDispatcher(new JsThreads<Envelope, MailboxesQueue>(new MailboxesQueue(), new Dispatch<Envelope>() {
            @Override
            public void dispatchMessage(Envelope message) {
                processEnvelope(message);
            }
        }));
    }
}