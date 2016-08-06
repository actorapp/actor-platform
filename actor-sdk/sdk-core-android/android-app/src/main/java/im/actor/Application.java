package im.actor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import im.actor.core.entity.Peer;
import im.actor.develop.R;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKApplication;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.conversation.attach.ShareMenuField;
import im.actor.sdk.controllers.conversation.attach.AbsAttachFragment;
import im.actor.sdk.controllers.conversation.attach.AttachFragment;
import im.actor.sdk.controllers.root.RootFragment;
import im.actor.sdk.controllers.settings.ActorSettingsCategories;
import im.actor.sdk.controllers.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.settings.ActorSettingsField;
import im.actor.sdk.controllers.settings.BaseActorSettingsActivity;
import im.actor.sdk.controllers.settings.BaseActorSettingsFragment;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

public class Application extends ActorSDKApplication {

    @Override
    public void onCreate() {
        MultiDex.install(this);
        super.onCreate();
    }

    @Override
    public void onConfigureActorSDK() {
        ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ActorSDK.sharedActor().setPushId(209133700967L);
        ActorSDK.sharedActor().setActorPushEndpoint(null);

        ActorStyle style = ActorSDK.sharedActor().style;
        style.setDialogsActiveTextColor(0xff5882ac);
        style.setShowAvatarPrivateInTitle(false);

        style.setDialogsActiveTextColor(0xff5882ac);
        style.setMainColor(Color.parseColor("#A43436"));
        style.setAccentColor(Color.parseColor("#1F3255"));
        style.setAccentPressedColor(Color.parseColor("#1F3255"));
        style.setPrimaryAltColor(Color.parseColor("#A43436"));
        style.setToolBarColor(Color.parseColor("#A43436"));
        style.setFabColor(Color.parseColor("#A43436"));

        ActorSDK.sharedActor().setFastShareEnabled(true);

        ActorSDK.sharedActor().setCallsEnabled(true);

        ActorSDK.sharedActor().setTosUrl("http://actor.im");
        ActorSDK.sharedActor().setPrivacyText("bla bla bla");

        ActorSDK.sharedActor().setVideoCallsEnabled(true);

        ActorSDK.sharedActor().setAutoJoinGroups(new String[]{
                "actor_news"
        });

        ActorSDK.sharedActor().setEndpoints(new String[]{"tcp://192.168.1.8:9070"});


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

        @Nullable
        @Override
        public Fragment fragmentForRoot() {
            return new RootFragment() {
                @Override
                public void onConfigureActionBar(ActionBar actionBar) {
                    super.onConfigureActionBar(actionBar);
                    actionBar.setDisplayShowHomeEnabled(true);
                    actionBar.setIcon(R.drawable.ic_app_notify);
                }
            };
        }

        @Nullable
        @Override
        public AbsAttachFragment fragmentForAttachMenu(Peer peer) {
            return new AttachFragment(peer) {

                @Override
                protected List<ShareMenuField> onCreateFields() {
                    List<ShareMenuField> res = super.onCreateFields();
                    res.add(new ShareMenuField(R.id.share_test, R.drawable.ic_edit_white_24dp, ActorSDK.sharedActor().style.getAccentColor(), "lol"));
                    return res;
                }

                @Override
                protected void onItemClicked(int id) {
                    super.onItemClicked(id);
                    if (id == R.id.share_test) {
                        Toast.makeText(getContext(), "Hey", Toast.LENGTH_LONG).show();
                    }
                }
            };
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
    }

}
