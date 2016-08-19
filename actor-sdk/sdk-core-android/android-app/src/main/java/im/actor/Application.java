package im.actor;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.content.PhotoContent;
import im.actor.develop.R;
import im.actor.holders.BubbleTextHolderLayouter;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKApplication;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.conversation.attach.ShareMenuField;
import im.actor.sdk.controllers.conversation.attach.AbsAttachFragment;
import im.actor.sdk.controllers.conversation.attach.AttachFragment;
import im.actor.sdk.controllers.conversation.messages.BubbleLayouter;
import im.actor.sdk.controllers.conversation.messages.JsonXmlBubbleLayouter;
import im.actor.sdk.controllers.conversation.messages.XmlBubbleLayouter;
import im.actor.sdk.controllers.conversation.messages.content.PhotoHolder;
import im.actor.sdk.controllers.conversation.messages.content.TextHolder;
import im.actor.sdk.controllers.conversation.messages.content.preprocessor.PreprocessedData;
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

        ActorStyle style = ActorSDK.sharedActor().style;
        style.setDialogsActiveTextColor(0xff5882ac);
        style.setShowAvatarPrivateInTitle(false);

        ActorSDK.sharedActor().setFastShareEnabled(true);

        ActorSDK.sharedActor().setCallsEnabled(true);

        ActorSDK.sharedActor().setTosUrl("http://actor.im");
        ActorSDK.sharedActor().setPrivacyText("bla bla bla");

        ActorSDK.sharedActor().setVideoCallsEnabled(true);

        ActorSDK.sharedActor().setAutoJoinGroups(new String[]{
                "actor_news"
        });


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

        @Override
        public void configureChatViewHolders(ArrayList<BubbleLayouter> layouters) {
//            layouters.add(0, new BubbleTextHolderLayouter());
            layouters.add(0, new XmlBubbleLayouter(content -> content instanceof PhotoContent, R.layout.adapter_dialog_photo, (adapter1, root1, peer1) -> new PhotoHolder(adapter1, root1, peer1) {
                @Override
                protected void onConfigureViewHolder() {
                    previewView.setColorFilter(ActorStyle.adjustColorAlpha(Color.CYAN, 20), PorterDuff.Mode.ADD);
                }
            }));
            layouters.add(0, new JsonXmlBubbleLayouter(null, R.layout.adapter_dialog_text, (adapter, root, peer) -> new TextHolder(adapter, root, peer) {
                @Override
                protected void bindData(Message message, long readDate, long receiveDate, boolean isUpdated, PreprocessedData preprocessedData) {
                    String jsonString = "can't read json";
                    try {
                        JSONObject jsonObject = new JSONObject(((JsonContent) message.getContent()).getRawJson());
                        String dataType = jsonObject.getString("dataType");
                        JSONObject data = jsonObject.getJSONObject("data");
                        jsonString = dataType + "\n\n";
                        jsonString += data.toString(3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    bindRawText(jsonString, readDate, receiveDate, reactions, message, false);
                }
            }));
        }

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
