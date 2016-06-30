package im.actor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.develop.R;
import im.actor.fragments.CustomRootFragment;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKApplication;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.conversation.ShareMenuField;
import im.actor.sdk.controllers.group.GroupInfoFragment;
import im.actor.sdk.controllers.profile.ProfileFragment;
import im.actor.sdk.controllers.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.settings.ActorSettingsField;
import im.actor.sdk.controllers.settings.BaseActorProfileActivity;
import im.actor.sdk.controllers.settings.BaseActorSettingsActivity;
import im.actor.sdk.controllers.settings.BaseActorSettingsFragment;
import im.actor.sdk.controllers.settings.BaseGroupInfoActivity;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

public class Application extends ActorSDKApplication {

    @Override
    public void onConfigureActorSDK() {
        ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ActorSDK.sharedActor().setPushId(209133700967L);

        ActorStyle style = ActorSDK.sharedActor().style;
        style.setDialogsActiveTextColor(0xff5882ac);
        ActorSDK.sharedActor().setFastShareEnabled(true);

        ActorSDK.sharedActor().setCallsEnabled(true);

        ActorSDK.sharedActor().setTosUrl("http://actor.im");
        ActorSDK.sharedActor().setPrivacyText("bla bla bla");

        ActorSDK.sharedActor().setVideoCallsEnabled(true);

//        ActorSDK.sharedActor().setTwitter("");
//        ActorSDK.sharedActor().setHomePage("http://www.foo.com");
//        ActorSDK.sharedActor().setInviteUrl("http://www.foo.com");
//        ActorSDK.sharedActor().setCallsEnabled(true);

//        ActorSDK.sharedActor().setEndpoints(new String[]{"tcp://192.168.1.184:9070"});

//        ActorStyle style = ActorSDK.sharedActor().style;
//        style.setMainColor(Color.parseColor("#529a88"));
//        style.setAvatarBackgroundResource(R.drawable.img_profile_avatar_default);
    }

    private class ActorSDKDelegate extends BaseActorSDKDelegate {

//        @Nullable
//        @Override
//        public Fragment fragmentForRoot() {
//            return new CustomRootFragment();
//        }

        //
        @Override
        public ArrayList<ShareMenuField> addCustomShareMenuFields() {
            ArrayList<ShareMenuField> shareMenuFields = new ArrayList<>();
            shareMenuFields.add(new ShareMenuField(R.drawable.conv_location_icon, ActorSDK.sharedActor().style.getAccentColor(), "lol", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Hey", Toast.LENGTH_LONG).show();
                }
            }));
            shareMenuFields.add(new ShareMenuField(R.drawable.conv_location_icon, ActorSDK.sharedActor().style.getMainColor(), "lol", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Hey", Toast.LENGTH_LONG).show();
                }
            }));
            shareMenuFields.add(new ShareMenuField(R.drawable.conv_location_icon, ActorSDK.sharedActor().style.getDividerColor(), "lol", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Hey", Toast.LENGTH_LONG).show();
                }
            }));
            shareMenuFields.add(new ShareMenuField(R.drawable.conv_location_icon, ActorSDK.sharedActor().style.getConvLikeColor(), "lol", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Hey", Toast.LENGTH_LONG).show();
                }
            }));
            return shareMenuFields;
        }
    }
}
