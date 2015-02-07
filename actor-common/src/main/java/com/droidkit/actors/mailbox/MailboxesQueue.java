package com.droidkit.actors.mailbox;

import com.droidkit.actors.dispatch.AbstractDispatchQueue;
import com.droidkit.actors.dispatch.DispatchResult;
import com.droidkit.actors.mailbox.collections.EnvelopeRoot;

/**
 * Queue of multiple mailboxes for MailboxesDispatcher
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public class MailboxesQueue extends AbstractDispatchQueue<Envelope> {

    private EnvelopeRoot envelopeRoot;

    public MailboxesQueue() {
        envelopeRoot = new EnvelopeRoot(this);
    }

    public EnvelopeRoot getEnvelopeRoot() {
        return envelopeRoot;
    }

    public void unlockMailbox(Mailbox mailbox) {
        envelopeRoot.attachCollection(mailbox.getEnvelopes());
    }

    public void disconnectMailbox(Mailbox mailbox) {
        envelopeRoot.detachCollection(mailbox.getEnvelopes());
    }

    public void notifyQueueChanged() {
        super.notifyQueueChanged();
    }

    @Override
    public DispatchResult dispatch(long time) {
        EnvelopeRoot.FetchResult res = envelopeRoot.fetchCollection(time);
        if (res == null) {
            return delay(FOREVER);
        }

        if (res.getEnvelope() != null) {
            DispatchResult result = result(res.getEnvelope());
            res.recycle();
            return result;
        } else {
            DispatchResult result = delay(res.getDelay());
            res.recycle();
            return result;
        }
    }


}
