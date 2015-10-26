package im.actor;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;

/**
 * Created by badgr on 16.10.2015.
 */
public class Application extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ActorSDK.sharedActor().setPushId(209133700967L);
        ActorSDK.sharedActor().createActor(this);
    }

    private class ActorSDKDelegate extends BaseActorSDKDelegate {
        @Override
        public View getBeforeNickSettingsView(Context context) {
            TextView tv = new TextView(context);
            tv.setText("BeforeNickSettingsView");
            return tv;
        }

        @Override
        public View getAfterPhoneSettingsView(Context context) {
            TextView tv = new TextView(context);
            tv.setText("AfterPhoneSettingsView");
            return tv;
        }

        @Override
        public View getSettingsTopView(Context context) {
            TextView tv = new TextView(context);
            tv.setText("SettingsTopView");
            return tv;
        }

        @Override
        public View getSettingsBottomView(Context context) {
            TextView tv = new TextView(context);
            tv.setText("SettingsBottomView");
            return tv;
        }

        @Override
        public ActorSettingsCategory getBeforeSettingsCategory() {
            return new ActorSettingsCategory() {
                @Override
                public String getCategoryName() {
                    return "BeforeSettingsCategory";
                }

                @Override
                public View getView(Context context) {
                    TextView tv = new TextView(context);
                    tv.setText("BeforeSettings");
                    return tv;
                }
            };
        }

        @Override
        public ActorSettingsCategory getAfterSettingsCategory() {
            return new ActorSettingsCategory() {
                @Override
                public String getCategoryName() {
                    return "AfterSettingsCategory";
                }

                @Override
                public View getView(Context context) {
                    TextView tv = new TextView(context);
                    tv.setText("AfterSettings");
                    return tv;
                }
            };
        }
    }
}
