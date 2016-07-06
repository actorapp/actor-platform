package im.actor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import im.actor.develop.R;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKApplication;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.conversation.ShareMenuField;
import im.actor.sdk.controllers.group.GroupInfoFragment;
import im.actor.sdk.controllers.profile.ProfileFragment;
import im.actor.sdk.controllers.settings.ActorSettingsCategories;
import im.actor.sdk.controllers.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.settings.ActorSettingsField;
import im.actor.sdk.controllers.settings.BaseActorProfileActivity;
import im.actor.sdk.controllers.settings.BaseActorSettingsActivity;
import im.actor.sdk.controllers.settings.BaseActorSettingsFragment;
import im.actor.sdk.controllers.settings.BaseGroupInfoActivity;
import im.actor.sdk.intents.ActorIntentFragmentActivity;
import im.actor.sdk.util.Screen;

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
//        AbsContent.registerConverter(new ContentConverter() {
//            @Override
//            public AbsContent convert(AbsContentContainer container) {
//                return JsonContent.convert(container, new TCBotMesaage());
//            }
//
//            @Override
//            public boolean validate(AbsContent content) {
//                return content instanceof TCBotMesaage;
//            }
//        });
    }

    private class ActorSDKDelegate extends BaseActorSDKDelegate {

//        @Override
//        public BaseJsonHolder getCustomMessageViewHolder(int dataTypeHash, MessagesAdapter messagesAdapter, ViewGroup viewGroup) {
//            if(dataTypeHash == "tcmessage".hashCode()){
//                return new TCMessageHolder(messagesAdapter, viewGroup, R.layout.tc_holder, false);
//            }
//            return null;
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

//        @Override
//        public BaseGroupInfoActivity getGroupInfoIntent(int gid) {
//            return new BaseGroupInfoActivity() {
//                @Override
//                public GroupInfoFragment getGroupInfoFragment(int chatId) {
//                    return GroupInfoEx.create(chatId);
//                }
//            };
//        }

        @Override
        public ActorIntentFragmentActivity getSettingsIntent() {
            return new BaseActorSettingsActivity() {
                @Override
                public BaseActorSettingsFragment getSettingsFragment() {
                    return new BaseActorSettingsFragment() {
                        CheckBox blablaCheckBox;

                        @Override
                        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                            blablaCheckBox = new CheckBox(getContext());
                            return super.onCreateView(inflater, container, savedInstanceState);
                        }

                        @Override
                        public ActorSettingsCategories getBeforeSettingsCategories() {
                            return new ActorSettingsCategories()
                                    .addCategory(new ActorSettingsCategory("azaza")
                                                    .addField(new ActorSettingsField(R.id.terminateSessions)
                                                                    .setName("blabla")
                                                                    .setIconResourceId(R.drawable.ic_edit_black_24dp)
                                                                    .setRightView(blablaCheckBox)
                                                    )
                                    );
                        }

                        @Override
                        public View.OnClickListener getMenuFieldOnClickListener() {
                            return new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    switch (v.getId()) {
                                        case R.id.terminateSessions:
                                            Toast.makeText(v.getContext(), "hey", Toast.LENGTH_LONG).show();
                                            blablaCheckBox.toggle();
                                            break;
                                    }
                                }
                            };
                        }

                    };
                }
            };
        }

//        @Override
//        public MainPhoneController getMainPhoneController(ActorMainActivity mainActivity) {
//            return new MainPhoneControllerEx(mainActivity);
//        }
    }

}
