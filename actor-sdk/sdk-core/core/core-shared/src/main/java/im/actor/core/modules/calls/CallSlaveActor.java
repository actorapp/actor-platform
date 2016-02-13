package im.actor.core.modules.calls;

import im.actor.core.api.ApiNeedOffer;
import im.actor.core.api.ApiWebRTCSignaling;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.Log;

public class CallSlaveActor extends CallActor {

    private static final String TAG = "CallSlaveActor";

    public CallSlaveActor(String busId, ModuleContext context) {
        super(busId, context);
    }

    @Override
    public void onSignalingMessage(int fromUid, long fromDeviceId, ApiWebRTCSignaling signaling) {
        if (signaling instanceof ApiNeedOffer) {
            ApiNeedOffer needOffer = (ApiNeedOffer) signaling;
            Log.w(TAG, "Need offer for: " + needOffer.getUid());
            getPeer(needOffer.getUid(), needOffer.getDevice()).send(new PeerConnectionActor.OnOfferNeeded());
        } else {
            super.onSignalingMessage(fromUid, fromDeviceId, signaling);
        }
    }
}
