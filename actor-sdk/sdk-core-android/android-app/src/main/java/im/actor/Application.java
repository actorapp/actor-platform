package im.actor;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsCategory;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsField;

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
        public BaseActorSettingsCategory[] getBeforeSettingsCategories() {
            BaseActorSettingsCategory[] settings = new BaseActorSettingsCategory[3];
            for (int i = 0; i < 3; i++) {
                final int finalI = i;
                settings[i] = new BaseActorSettingsCategory() {
                    @Override
                    public String getCategoryName() {
                        return "BeforeSettingsCategory" + finalI;
                    }

                    @Override
                    public View getView(Context context) {
                        TextView tv = new TextView(context);
                        tv.setText("BeforeSettings" + finalI);
                        return tv;
                    }
                };
            }
            return settings;
        }

        @Override
        public BaseActorSettingsCategory[] getAfterSettingsCategories() {
            return new BaseActorSettingsCategory[]{
                    new BaseActorSettingsCategory() {
                        @Override
                        public String getCategoryName() {
                            return "AfterSettingsCategory";
                        }

                        @Override
                        public BaseActorSettingsField[] getFields() {
                            return new BaseActorSettingsField[]{
                                    new BaseActorSettingsField() {

                                        @Override
                                        public int getIconResourceId() {
                                            return R.drawable.ic_cloud_download_white_36dp;
                                        }

                                        @Override
                                        public String getName() {
                                            return "Field";
                                        }

                                        @Override
                                        public View.OnClickListener getOnclickListener() {
                                            return new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Toast.makeText(getApplicationContext(), "Yo", Toast.LENGTH_SHORT).show();
                                                }
                                            };
                                        }

                                        @Override
                                        public int getIconColor() {
                                            return getResources().getColor(R.color.accent);
                                        }

                                        @Override
                                        public View getRightView(Context context) {
                                            return new SwitchCompat(context);
                                        }
                                    },

                                    new BaseActorSettingsField() {
                                        @Override
                                        public boolean addBottomDivider() {
                                            return false;
                                        }

                                        @Override
                                        public View getView(Context context) {
                                            TextView tv = new TextView(context);
                                            tv.setText("Custom");
                                            return tv;
                                        }
                                    }
                            };
                        }
                    }
            };
        }
    }
}
