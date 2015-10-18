package im.actor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import im.actor.sdk.ActorSDK;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActorSDK.sharedActor().startMessagingApp(this);
        finish();
    }
}
