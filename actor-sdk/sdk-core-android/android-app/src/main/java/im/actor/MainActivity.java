package im.actor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import im.actor.core.AuthState;
import im.actor.core.network.mtp.entity.rpc.Push;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.push.Utils;
import im.actor.tour.TourActivity;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(MainActivity.this, "api_key"));

        ActorSDK.sharedActor().waitForReady();

        if (!messenger().isLoggedIn()) {
            startActivity(new Intent(this, TourActivity.class));
            finish();
            return;
        }

        ActorSDK.sharedActor().startMessagingApp(this);
        finish();
    }
}
