package im.actor;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKApplication;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.fragment.auth.BaseAuthFragment;
import im.actor.sdk.controllers.fragment.auth.SignEmailFragment;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsField;
import im.actor.sdk.intents.ActorIntent;

public class Application extends ActorSDKApplication {

    @Override
    public void onConfigureActorSDK() {
        ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ActorSDK.sharedActor().setPushId(209133700967L);
        ActorSDK.sharedActor().createActor(this);

        ActorStyle style = ActorSDK.sharedActor().style;
        style.setMainColor(Color.parseColor("#529a88"));
    }

    private class ActorSDKDelegate extends BaseActorSDKDelegate {
        @Override
        public BaseAuthFragment getSignFragment() {
            return new SignEmailFragment();
        }
    }
}
