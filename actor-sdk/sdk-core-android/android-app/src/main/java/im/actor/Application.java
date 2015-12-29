package im.actor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.ContentConverter;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.content.internal.AbsContentContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.runtime.json.JSONException;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKApplication;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.conversation.messages.BaseCustomHolder;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.MessagesFragment;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsField;
import im.actor.sdk.controllers.fragment.settings.BaseActorChatActivity;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsActivity;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsFragment;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

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
                return JsonContent.convert(container, Custom.class);
            }

            @Override
            public Class destinationType() {
                return Custom.class;
            }
        });
        AbsContent.registerConverter(new ContentConverter() {
            @Override
            public AbsContent convert(AbsContentContainer container) {
                return JsonContent.convert(container, CustomTwo.class);
            }

            @Override
            public Class destinationType() {
                return CustomTwo.class;
            }
        });
    }

    public static class Custom extends JsonContent {

        public String getText() {
            return getSimpleStringData();
        }

        @Override
        public String getDataType() {
            return "customJson";
        }

        @Override
        public String getContentDescriptionEn() {
            return "custom json msg";
        }

        @Override
        public String getContentDescriptionRu() {
            return " бла бла бла";
        }
    }

    public static class CustomTwo extends JsonContent {

        public String getText() {
            return getSimpleStringData();
        }

        @Override
        public String getDataType() {
            return "customTwoJson";
        }

        @Override
        public String getContentDescriptionEn() {
            return "custom json msg";
        }

        @Override
        public String getContentDescriptionRu() {
            return " бла бла бла";
        }
    }



    private class ActorSDKDelegate extends BaseActorSDKDelegate {

        @Override
        public ActorIntent getChatIntent() {
            return new BaseActorChatActivity() {
                @Override
                public MessagesFragment getChatFragment(Peer peer) {
                    return new MessagesFragment(peer) {
                        @Override
                        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//                            messenger().sendCustomJsonMessage(getPeer(), JsonContent.create(Custom.class, System.currentTimeMillis()+"One"));
//                            messenger().sendCustomJsonMessage(getPeer(), JsonContent.create(CustomTwo.class, System.currentTimeMillis()+"Two"));
//                            messenger().sendCustomJsonMessage(getPeer(), JsonContent.create(CustomTwo.class, System.currentTimeMillis()+"Two"));
                            return super.onCreateView(inflater, container, savedInstanceState);
                        }
                    };
                }
            };
        }

        @Override
        public BaseCustomHolder getCustomMessageViewHolder(Class<AbsContent> content, MessagesAdapter messagesAdapter, ViewGroup viewGroup) {
            if (content.equals(Custom.class)) {
                return new CustomHolder(messagesAdapter, viewGroup, R.layout.custom_holder, false);
            } else if (content.equals(CustomTwo.class)) {
                return new CustomTwoHolder(messagesAdapter, viewGroup, R.layout.custom_holder, false);

            } else {
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
