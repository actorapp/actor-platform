package com.droidkit.actors;

import com.droidkit.actors.mailbox.Mailbox;
import com.droidkit.actors.mailbox.MailboxesQueue;

/**
 * Creator of mailbox for Actor
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public interface MailboxCreator {
    /**
     * Creating of mailbox in queue
     *
     * @param queue mailbox queue
     * @return mailbox
     */
    public Mailbox createMailbox(MailboxesQueue queue);
}
