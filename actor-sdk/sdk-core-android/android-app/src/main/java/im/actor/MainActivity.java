package im.actor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.hwid.HuaweiId;
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.TokenResult;
import com.xiaomi.mipush.sdk.MiPushClient;

import im.actor.core.AuthState;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.push.Utils;
import im.actor.tour.TourActivity;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class MainActivity extends AppCompatActivity {

    static HuaweiApiClient client;
    // user your appid the key.
    private static final String APP_ID = "2882303761517562000";
    // user your appid the key.
    private static final String APP_KEY = "5731756231000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int phoneFlag = Utils.isWhatPhone();
        if (phoneFlag == 0) {
//百度推送
            PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY,
                    Utils.getMetaValue(this, "api_key"));
        } else if (phoneFlag == 1) {
// 华为推送
            HuaweiIdSignInOptions options = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN)
                    .build();
            huaWeiCallBack callBack = new huaWeiCallBack(this);
            client = new HuaweiApiClient.Builder(this) //
                    .addApi(HuaweiId.SIGN_IN_API, options)//
                    .addConnectionCallbacks(callBack) //
                    .addOnConnectionFailedListener(callBack) //
                    .build();
            client.connect();
        } else if (phoneFlag == 2) {
            //小米推送
//            注意：因为推送服务XMPushService在AndroidManifest.xml中设置为运行在另外一个进程，这导致本Application会被实例化两次，所以我们需要让应用的主进程初始化。
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }

        ActorSDK.sharedActor().waitForReady();

        if (!messenger().isLoggedIn()) {
            startActivity(new Intent(this, TourActivity.class));
            finish();
            return;
        }

        ActorSDK.sharedActor().startMessagingApp(this);
        finish();
    }


    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static boolean mResolvingError = false;

    private static class huaWeiCallBack implements HuaweiApiClient.ConnectionCallbacks, HuaweiApiClient.OnConnectionFailedListener,
            HuaweiApiAvailability.OnUpdateListener {
        Context context;

        public huaWeiCallBack(Context context) {
            this.context = context;
        }

        @Override
        public void onUpdateFailed(@NonNull ConnectionResult connectionResult) {
            Log.i("PushMoa", "更新失败");
        }

        @Override
        public void onConnected() {
            getToken(client);
            Log.i("PushMoa", "连接成功");
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i("PushMoa", "连接暂停");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult result) {
            Toast.makeText(context, "连接失败" + result.getErrorCode(), Toast.LENGTH_SHORT).show();

            Log.i("PushMoa", "onConnectionFailed, ErrorCode: " + result.getErrorCode());

            if (mResolvingError) {
                return;
            }

            int errorCode = result.getErrorCode();
            HuaweiApiAvailability availability = HuaweiApiAvailability.getInstance();

            if (availability.isUserResolvableError(errorCode)) {
                mResolvingError = true;
                availability.resolveError((Activity) context, errorCode, REQUEST_RESOLVE_ERROR, this);
            }
        }
    }

    private static void getToken(final HuaweiApiClient client) {
        if (client == null || !client.isConnected()) {
//            Toast.makeText(context, "连接失败，不请求token", Toast.LENGTH_SHORT).show();
            return;
        }
        // 异步调用方式
        new Thread() {
            @Override
            public void run() {
                super.run();
                PendingResult<TokenResult> tokenResult = HuaweiPush.HuaweiPushApi.getToken(client);
                tokenResult.await();
            }
        }.start();
    }


}
