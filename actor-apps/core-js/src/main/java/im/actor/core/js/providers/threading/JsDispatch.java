/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.providers.threading;


import im.actor.core.droidkit.actors.ActorSystem;
import im.actor.core.droidkit.actors.dispatch.Dispatch;
import im.actor.core.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.core.droidkit.actors.mailbox.Envelope;
import im.actor.core.droidkit.actors.mailbox.MailboxesQueue;

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