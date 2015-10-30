package im.actor;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.BaseActorSDKDelegate;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsField;

public class Application extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        ActorSDK.sharedActor().setDelegate(new ActorSDKDelegate());
        ActorSDK.sharedActor().setPushId(209133700967L);
        ActorSDK.sharedActor().createActor(this);

        ActorStyle style = ActorSDK.sharedActor().style;
//        style.setMainColor(Color.parseColor("#529a88"));
        style.setMainColor(Color.CYAN);
        style.setMainBackground(Color.DKGRAY);
        style.setTextPrimary(Color.GREEN);
        style.setTextSecondary(Color.RED);
        style.setSettingsIcon(Color.GREEN);
        style.setDivider(Color.WHITE);
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
        public ActorSettingsCategory[] getBeforeSettingsCategories() {
            ActorSettingsCategory[] settings = new ActorSettingsCategory[3];
            for (int i = 0; i < 3; i++) {
                final int finalI = i;
                settings[i] = new ActorSettingsCategory() {
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
        public ActorSettingsCategory[] getAfterSettingsCategories() {
            return new ActorSettingsCategory[]{
                    new ActorSettingsCategory() {
                        @Override
                        public String getCategoryName() {
                            return "AfterSettingsCategory";
                        }

                        @Override
                        public ActorSettingsField[] getFields() {

                            final ActorSettingsField zero = new ActorSettingsField() {
                                @Override
                                public View getRightView(Context context) {
                                    return new CheckBox(context);
                                }
                            }
                                    .setIconResourceId(R.drawable.ic_notifications_white_18dp)
                                    .setName("Zero!");
                            zero.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CheckBox chb = (CheckBox) zero.getRightView();
                                    chb.setChecked(!chb.isChecked());
                                }
                            });

                            return new ActorSettingsField[]{

                                    zero,

                                    new ActorSettingsField("Field", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(getApplicationContext(), "Yo", Toast.LENGTH_SHORT).show();
                                        }
                                    }, R.drawable.ic_cloud_download_white_36dp, getResources().getColor(R.color.accent)),

                                    new ActorSettingsField() {

                                        @Override
                                        public View getView(Context context) {
                                            TextView tv = new TextView(context);
                                            tv.setText("Custom");
                                            return tv;
                                        }
                                    },

                                    new ActorSettingsField("Field 3", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(getApplicationContext(), "Yo 3", Toast.LENGTH_SHORT).show();
                                        }
                                    }, R.drawable.ic_camera_alt_white_24dp, getResources().getColor(R.color.action)) {

                                        @Override
                                        public View getRightView(Context context) {
                                            return new SwitchCompat(context);
                                        }
                                    },

                                    new ActorSettingsField(false) {

                                        @Override
                                        public String getName() {
                                            return "Field 4";
                                        }

                                        @Override
                                        public View.OnClickListener getOnclickListener() {
                                            return new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Toast.makeText(getApplicationContext(), "Yo 4", Toast.LENGTH_SHORT).show();
                                                }
                                            };
                                        }

                                        @Override
                                        public View getRightView(Context context) {
                                            return new AppCompatCheckBox(context);
                                        }
                                    },
                            };
                        }
                    }
            };
        }
    }
}
