package im.actor;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.ContentConverter;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.content.internal.AbsContentContainer;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKApplication;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.conversation.messages.BaseCustomHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsField;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsActivity;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsFragment;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

public class Application extends ActorSDKApplication {

    @Override
    public void onConfigureActorSDK() {
        ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ActorSDK.sharedActor().setPushId(209133700967L);

//        ArrayList<String> endpoints = new ArrayList<String>();
//        endpoints.add("foo");
//        ActorSDK.sharedActor().setEndpoints(endpoints);

        ActorStyle style = ActorSDK.sharedActor().style;
        style.setMainColor(Color.parseColor("#529a88"));
        AbsContent.registerConverter(new ContentConverter() {
            @Override
            public AbsContent convert(AbsContentContainer container) {
                return JsonContent.convert(container, new TCBotMesaage());
            }

            @Override
            public boolean validate(AbsContent content) {
                return content instanceof TCBotMesaage;
            }
        });
    }

    public static class TCBotMesaage extends JsonContent {

        @Override
        public String getDataType() {
            return "tcmessage";
        }

        @Override
        public String getContentDescriptionEn() {
            return "TeamCity message";
        }

        @Override
        public String getContentDescriptionRu() {
            return null;
        }

        @Override
        public String getContentDescriptionPt() {
            return null;
        }


        @Override
        public String getContentDescriptionAr() {
            return null;
        }

        @Override
        public String getContentDescriptionCn() {
            return null;
        }

        @Override
        public String getContentDescriptionEs() {
            return null;
        }
    }




    private class ActorSDKDelegate extends BaseActorSDKDelegate {

        @Override
        public BaseCustomHolder getCustomMessageViewHolder(int id, MessagesAdapter messagesAdapter, ViewGroup viewGroup) {
            switch (id) {
                case 0:
                    return new TCMessageHolder(messagesAdapter, viewGroup, R.layout.custom_holder, false);

                default:
                    return null;
            }
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
}
