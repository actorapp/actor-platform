package im.actor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.ArrayList;

import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.ContentConverter;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.content.internal.AbsContentContainer;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKApplication;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.conversation.messages.BaseJsonHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.fragment.group.GroupInfoFragment;
import im.actor.sdk.controllers.fragment.profile.ProfileFragment;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsField;
import im.actor.sdk.controllers.fragment.settings.BaseActorProfileActivity;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsActivity;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsFragment;
import im.actor.sdk.controllers.fragment.settings.BaseGroupInfoActivity;
import im.actor.sdk.intents.ActorIntentFragmentActivity;
import im.actor.tc.TCMessageHolder;

public class Application extends ActorSDKApplication {

    @Override
    public void onConfigureActorSDK() {
        ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ActorSDK.sharedActor().setPushId(209133700967L);
        ActorSDK.sharedActor().setTwitter("");
        ActorSDK.sharedActor().setHomePage("http://www.foo.com");
        ActorSDK.sharedActor().setInviteUrl("http://www.foo.com");
        ActorSDK.sharedActor().setCallsEnabled(true);

//        ActorSDK.sharedActor().setEndpoints(new String[]{"tcp://192.168.1.184:9070"});

        ActorStyle style = ActorSDK.sharedActor().style;
        style.setMainColor(Color.parseColor("#529a88"));
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

        @Override
        public BaseJsonHolder getCustomMessageViewHolder(int id, MessagesAdapter messagesAdapter, ViewGroup viewGroup) {
            switch (id) {
                case 0:
                    return new TCMessageHolder(messagesAdapter, viewGroup, R.layout.tc_holder, false);

                default:
                    return null;
            }
        }

        @Override
        public BaseActorProfileActivity getProfileIntent(int uid) {
            return new BaseActorProfileActivity() {
                @Override
                public ProfileFragment getProfileFragment(int uid) {
                    return ProfileFragmentEx.create(uid);
                }
            };
        }

        @Override
        public BaseGroupInfoActivity getGroupInfoIntent(int gid) {
            return new BaseGroupInfoActivity() {
                @Override
                public GroupInfoFragment getGroupInfoFragment(int chatId) {
                    return GroupInfoEx.create(chatId);
                }
            };
        }

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

    public static class ProfileFragmentEx extends ProfileFragment {

        public static ProfileFragment create(int uid) {
            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_UID, uid);
            ProfileFragment res = new ProfileFragmentEx();
            res.setArguments(bundle);
            return res;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            view.findViewById(R.id.docsContainer).setVisibility(View.VISIBLE);
            view.findViewById(R.id.sharedContainer).setVisibility(View.VISIBLE);
            return view;
        }
    }

    public static class GroupInfoEx extends GroupInfoFragment {

        public static GroupInfoFragment create(int chatId) {
            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_CHAT_ID, chatId);
            GroupInfoFragment res = new GroupInfoEx();
            res.setArguments(bundle);
            return res;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            header.findViewById(R.id.docsContainer).setVisibility(View.VISIBLE);
            header.findViewById(R.id.sharedContainer).setVisibility(View.VISIBLE);
            return view;
        }
    }
}
