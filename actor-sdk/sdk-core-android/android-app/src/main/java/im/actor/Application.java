package im.actor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;

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
//        @Override
//        public ArrayList<ShareMenuField> addCustomShareMenuFields() {
//            ArrayList<ShareMenuField> shareMenuFields = new ArrayList<>();
//            shareMenuFields.add(new ShareMenuField(R.drawable.conv_location_icon, ActorSDK.sharedActor().style.getAccentColor(), "lol", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), "Hey", Toast.LENGTH_LONG).show();
//                }
//            }));
//            return shareMenuFields;
//        }

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

                        @Override
                        public View getBeforeNickSettingsView() {
                            return null;
                        }

                        @Override
                        public View getAfterPhoneSettingsView() {
                            return null;
                        }

                        @Override
                        public View getSettingsTopView() {
                            return null;
                        }

                        @Override
                        public View getSettingsBottomView() {
                            return null;
                        }

                        @Override
                        public boolean showWallpaperCategory() {
                            return true;
                        }

                        @Override
                        public boolean showAskQuestion() {
                            return true;
                        }

                        @Override
                        public ActorSettingsCategory[] getBeforeSettingsCategories() {
                            return new ActorSettingsCategory[]{
                                    new ActorSettingsCategory() {

//                                        @Override
//                                        public int getIconResourceId() {
//                                            return R.drawable.ic_notifications_white_18dp;
//                                        }

                                        @Override
                                        public String getCategoryName() {
                                            return "test";
                                        }

                                        @Override
                                        public ActorSettingsField[] getFields() {
                                            final CheckBox chb = new CheckBox(getContext());
                                            ActorSettingsField field = new ActorSettingsField() {
                                                @Override
                                                public View getRightView() {
                                                    return chb;
                                                }

                                                @Override
                                                public String getName() {
                                                    return "azazaz";
                                                }
                                            };
                                            field.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    chb.setChecked(!chb.isChecked());
                                                }
                                            });
                                            return new ActorSettingsField[]{
                                                    field
                                            };
                                        }
                                    }
                            };
                        }

                        @Override
                        public ActorSettingsCategory[] getAfterSettingsCategories() {
                            return new ActorSettingsCategory[0];
                        }
                    };
                }
            };
        }
    }

}
