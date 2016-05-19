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
            if (object.has("data")) {
                JSONObject data = object.getJSONObject("data");
                ActorSDK.sharedActor().waitForReady();
                if (data.has("seq")) {
                    int seq = data.getInt("seq");
                    Log.d("SDKPushReceiver", "Seq Received: " + seq);
                    ActorSDK.sharedActor().getMessenger().onPushReceived(seq);
                } else if (data.has("callId")) {
                    Long callId = Long.parseLong(data.getString("callId"));
                    int attempt = 0;
                    if (data.has("attemptIndex")) {
                        attempt = data.getInt("attemptIndex");
                    }
                    Log.d("SDKPushReceiver", "Received Call #" + callId + " (" + attempt + ")");
                    ActorSDK.sharedActor().getMessenger().checkCall(callId, attempt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}