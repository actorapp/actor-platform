/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import im.actor.runtime.actors.mailbox.Mailbox;
import im.actor.runtime.actors.mailbox.MailboxesQueue;

/**
 * Creator of mailbox for Actor
 */
public interface MailboxCreator {
    /**
     * Creating of mailbox in queue
     *
     * @param queue mailbox queue
     * @return mailbox
     */
    Mailbox createMailbox(MailboxesQueue queue);
}
