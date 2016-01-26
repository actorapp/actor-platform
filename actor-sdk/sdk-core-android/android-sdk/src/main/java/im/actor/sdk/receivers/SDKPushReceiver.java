package im.actor.sdk.receivers;

import im.actor.runtime.Log;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.push.ActorPushReceiver;

public class SDKPushReceiver extends ActorPushReceiver {

    @Override
    public void onPushReceived(String payload) {
        try {
            JSONObject object = new JSONObject(payload);
            int seq = object.getJSONObject("data").getInt("seq");
            Log.d("SDKPushReceiver", "Seq Received: " + seq);
            ActorSDK.sharedActor().getMessenger().onPushReceived(seq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}