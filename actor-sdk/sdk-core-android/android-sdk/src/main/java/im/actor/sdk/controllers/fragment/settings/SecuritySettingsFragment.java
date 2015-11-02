package im.actor.sdk.controllers.fragment.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.actor.core.api.ApiAuthHolder;
import im.actor.core.api.ApiAuthSession;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.fragment.BaseFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SecuritySettingsFragment extends BaseFragment {

    private TextView loading;
    private LinearLayout authItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fr_settings_encryption, container, false);
        ((TextView) res.findViewById(R.id.security_settings_title)).setTextColor(ActorSDK.sharedActor().style.getSettingsMainTitleColor());
        loading = (TextView) res.findViewById(R.id.loading);
        loading.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        loading.setVisibility(View.GONE);
        loading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLoad();
            }
        });
        authItems = (LinearLayout) res.findViewById(R.id.authItems);
        res.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

        res.findViewById(R.id.terminateSessions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.security_terminate_message)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                execute(messenger().terminateAllSessions(), R.string.progress_common,
                                        new CommandCallback<Boolean>() {
                                            @Override
                                            public void onResult(Boolean res) {
                                                performLoad();
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                performLoad();
                                                Toast.makeText(getActivity(),
                                                        "Unable to remove auth", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton(R.string.dialog_no, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
            }
        });
        ((TextView) res.findViewById(R.id.settings_terminate_sessions_title)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.settings_terminate_sessions_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        performLoad();

        return res;
    }

    private void performLoad() {
        loading.setText("Loading...");
        loading.setClickable(true);
        showView(loading, false);

        executeSilent(messenger().loadSessions(), new CommandCallback<List<ApiAuthSession>>() {
            @Override
            public void onResult(List<ApiAuthSession> res) {
                goneView(loading, false);
                authItems.removeAllViews();
                ArrayList<ApiAuthSession> items = new ArrayList<ApiAuthSession>(res);
                Collections.sort(items, new Comparator<ApiAuthSession>() {
                    @Override
                    public int compare(ApiAuthSession lhs, ApiAuthSession rhs) {
                        return rhs.getAuthTime() - lhs.getAuthTime();
                    }
                });
                for (final ApiAuthSession item : items) {
                    if (getActivity() == null) return;
                    View view = getActivity().getLayoutInflater().inflate(R.layout.adapter_auth, authItems, false);

                    boolean isThisDevice = item.getAuthHolder() == ApiAuthHolder.OTHERDEVICE;
                    String deviceTitle = (isThisDevice ? "(This) " : "") + item.getDeviceTitle();
                    ((TextView) view.findViewById(R.id.date)).setText(messenger().getFormatter().formatShortDate(item.getAuthTime() * 1000L));
                    ((TextView) view.findViewById(R.id.date)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
                    ((TextView) view.findViewById(R.id.appTitle)).setText(item.getAppTitle());
                    ((TextView) view.findViewById(R.id.appTitle)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
                    ((TextView) view.findViewById(R.id.deviceTitle)).setText(deviceTitle);
                    ((TextView) view.findViewById(R.id.deviceTitle)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
                    if (!isThisDevice) {
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage("Are you sure want to logout " + item.getDeviceTitle() + " device? All data will be lost on this device.")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                execute(messenger().terminateSession(item.getId()), R.string.progress_common,
                                                        new CommandCallback<Boolean>() {
                                                            @Override
                                                            public void onResult(Boolean res) {
                                                                performLoad();
                                                            }

                                                            @Override
                                                            public void onError(Exception e) {
                                                                Toast.makeText(getActivity(), "Unable to remove auth", Toast.LENGTH_SHORT).show();
                                                                performLoad();
                                                            }
                                                        });
                                            }
                                        })
                                        .setNegativeButton("No", null)
                                        .show()
                                        .setCanceledOnTouchOutside(true);
                            }
                        });
                    }
                    authItems.addView(view);
                }
            }

            @Override
            public void onError(Exception e) {
                loading.setText("Unable to load. Press to try again.");
                loading.setClickable(true);
                showView(loading, false);
            }
        });
    }
}
