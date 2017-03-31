package im.actor.sdk.push;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;

import java.util.List;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;

/**
 * 1、PushMessageReceiver 是个抽象类，该类继承了 BroadcastReceiver。<br/>
 * 2、需要将自定义的 DemoMessageReceiver 注册在 AndroidManifest.xml 文件中：
 * <pre>
 * {@code
 *  <receiver
 *      android:name="com.xiaomi.mipushdemo.DemoMessageReceiver"
 *      android:exported="true">
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.RECEIVE_MESSAGE" />
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.MESSAGE_ARRIVED" />
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="com.xiaomi.mipush.ERROR" />
 *      </intent-filter>
 *  </receiver>
 *  }</pre>
 * 3、DemoMessageReceiver 的 onReceivePassThroughMessage 方法用来接收服务器向客户端发送的透传消息。<br/>
 * 4、DemoMessageReceiver 的 onNotificationMessageClicked 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法会在用户手动点击通知后触发。<br/>
 * 5、DemoMessageReceiver 的 onNotificationMessageArrived 方法用来接收服务器向客户端发送的通知消息，
 * 这个回调方法是在通知消息到达客户端时触发。另外应用在前台时不弹出通知的通知消息到达客户端也会触发这个回调函数。<br/>
 * 6、DemoMessageReceiver 的 onCommandResult 方法用来接收客户端向服务器发送命令后的响应结果。<br/>
 * 7、DemoMessageReceiver 的 onReceiveRegisterResult 方法用来接收客户端向服务器发送注册命令后的响应结果。<br/>
 * 8、以上这些方法运行在非 UI 线程中。
 *
 * @author mayixiang
 */
public class XiaoMiMessageReceiver extends com.xiaomi.mipush.sdk.PushMessageReceiver {

    private String mRegId;
    private String mTopic;
    private String mAlias;
    private String mAccount;
    private String mStartTime;
    private String mEndTime;

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        try {
            String str = message.getContent();
            Utils.writeTxtToFile("收到一条Push消息：" + str, "log.txt");
            im.actor.runtime.json.JSONObject object = new im.actor.runtime.json.JSONObject(str);
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
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        String log = message.getContent();
        System.out.println(log);
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        try {
            String str = message.getContent();
            Utils.writeTxtToFile("收到一条onNotificationMessageArrived消息：" + str, "log.txt");
            im.actor.runtime.json.JSONObject object = new im.actor.runtime.json.JSONObject(str);
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
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
            } else {
//                log = context.getString(R.string.register_fail);
            }
        }
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
//        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                String url = "http://127.0.0.1:8080/ActorPush/getMessage" + "?pushType=xiaomi&id=" + Uri.encode(cmdArg1, "UTF-8");
                ActorSDK.sharedActor().getMessenger().registerActorPush(url);
//                log = context.getString(R.string.register_success);
            } else {
//                log = context.getString(R.string.register_fail);
            }
        }

    }


}
