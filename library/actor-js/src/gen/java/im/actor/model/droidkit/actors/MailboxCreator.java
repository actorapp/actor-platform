package im.actor.model.droidkit.actors;

import im.actor.model.droidkit.actors.mailbox.Mailbox;
import im.actor.model.droidkit.actors.mailbox.MailboxesQueue;

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
