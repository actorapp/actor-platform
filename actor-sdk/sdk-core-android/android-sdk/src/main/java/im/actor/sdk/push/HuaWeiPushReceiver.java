package im.actor.sdk.push;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.huawei.hms.support.api.push.PushReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import im.actor.sdk.ActorSDK;

/*
 * 接收Push所有消息的广播接收器
 */
public class HuaWeiPushReceiver extends PushReceiver {

    @Override
    public void onPushState(Context context, boolean b) {
        super.onPushState(context, b);
        try {
            String content = "查询push通道状态： " + (b ? "已连接" : "未连接");
            Log.d("PushLog", content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onToken(Context context, String s) {
        super.onToken(context, s);
        Log.i("PushMoa", "进入token1");
    }

    @Override
    public void onToken(Context context, String token, Bundle extras) {
        Log.i("PushMoa", "进入token2:" + token);

        String url = "http://127.0.0.1:8080/ActorPush/getMessage" + "?pushType=huawei&id=" + token;
        ActorSDK.sharedActor().getMessenger().registerActorPush(url);
    }


    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
            Toast.makeText(context.getApplicationContext(), "收到一条Push消息：", Toast.LENGTH_LONG).show();
            String message = new String(msg, "UTF-8");

            Utils.writeTxtToFile("收到一条Push消息："+message, "log.txt");
            try {
                im.actor.runtime.json.JSONObject object = new im.actor.runtime.json.JSONObject(message);
                if (object.has("data")) {
                    im.actor.runtime.json.JSONObject data = object.getJSONObject("data");
                    ActorSDK.sharedActor().waitForReady();
                    if (data.has("seq")) {
                        int seq = data.getInt("seq");
                        int authId = data.optInt("authId");
                        ActorSDK.sharedActor().getMessenger().onPushReceived(seq, authId);
                    } else if (data.has("callId")) {
                        Long callId = Long.parseLong(data.getString("callId"));
                        int attempt = 0;
                        if (data.has("attemptIndex")) {
                            attempt = data.getInt("attemptIndex");
                        }
                        im.actor.runtime.Log.d("SDKPushReceiver", "Received Call #" + callId + " (" + attempt + ")");
                        ActorSDK.sharedActor().getMessenger().checkCall(callId, attempt);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onEvent(Context context, Event event, Bundle extras) {
        Log.i("PushMoa", "进入onEvent");
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
//            int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
//            if (0 != notifyId) {
//                NotificationManager manager = (NotificationManager) context
//                        .getSystemService(Context.NOTIFICATION_SERVICE);
//                manager.cancel(notifyId);
//            }
////            String content = "收到通知附加消息： " + extras.getString(BOUND_KEY.pushMsgKey);
////            Log.i("PushMoa", "进入onEvent"+content);
//            MyPushMessageReceiver.clickedEvent(context,"","",extras.getString(BOUND_KEY.pushMsgKey));
        }
        super.onEvent(context, event, extras);
    }

}
