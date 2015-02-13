package im.actor.messenger.app.fragment.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import im.actor.api.scheme.AuthSession;
import im.actor.api.scheme.rpc.ResponseGetAuthSessions;
import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.Formatter;
import im.actor.messenger.app.view.SecurityKey;
import im.actor.messenger.core.actors.auth.AuthController;
import im.actor.messenger.core.actors.base.UiAskCallback;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 09.10.14.
 */
public class SecuritySettingsFragment extends BaseCompatFragment {

    private TextView loading;
    private LinearLayout authItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fr_settings_encryption, container, false);
        loading = (TextView) res.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        loading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLoad();
            }
        });
        authItems = (LinearLayout) res.findViewById(R.id.authItems);

        res.findViewById(R.id.showMyKey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecurityKey.showKey(users().get(myUid()).getRaw().getKeyHashes(), getActivity());
            }
        });

        res.findViewById(R.id.terminateSessions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.security_terminate_message)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeAuth();
                            }
                        })
                        .setNegativeButton(R.string.dialog_no, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
            }
        });

        performLoad();

        return res;
    }

    private void performLoad() {
        ask(AuthController.authController().requestAuth(), new UiAskCallback<ResponseGetAuthSessions>() {
            @Override
            public void onPreStart() {
                loading.setText("Loading...");
                loading.setClickable(false);
                authItems.removeAllViews();
                showView(loading, false);
            }

            @Override
            public void onCompleted(ResponseGetAuthSessions res) {
                goneView(loading, false);
                authItems.removeAllViews();
                ArrayList<AuthSession> items = new ArrayList<AuthSession>(res.getUserAuths());
                Collections.sort(items, new Comparator<AuthSession>() {
                    @Override
                    public int compare(AuthSession lhs, AuthSession rhs) {
                        return rhs.getAuthTime() - lhs.getAuthTime();
                    }
                });
                for (final AuthSession item : items) {
                    View view = getActivity().getLayoutInflater().inflate(R.layout.adapter_auth, authItems, false);

                    boolean isThisDevice = item.getAuthHolder() == 0;
                    String deviceTitle = (isThisDevice ? "(This) " : "") + item.getDeviceTitle();
                    ((TextView) view.findViewById(R.id.date)).setText(Formatter.formatShortDate(item.getAuthTime() * 1000L));
                    ((TextView) view.findViewById(R.id.appTitle)).setText(item.getAppTitle());
                    ((TextView) view.findViewById(R.id.deviceTitle)).setText(deviceTitle);
                    if (!isThisDevice) {
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage("Are you sure want to logout " + item.getDeviceTitle() + " device? All data will be lost on this device.")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                removeAuth(item);
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
            public void onError(Throwable t) {
                loading.setText("Unable to load. Press to try again.");
                loading.setClickable(true);
                showView(loading, false);
            }
        });
    }

    private void removeAuth(AuthSession item) {
        ask(AuthController.authController().removeAuth(item), new UiAskCallback<Boolean>() {

            private ProgressDialog progressDialog;

            @Override
            public void onPreStart() {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Terminating auth...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            public void onCompleted(Boolean res) {
                progressDialog.dismiss();
                performLoad();
            }

            @Override
            public void onError(Throwable t) {
                progressDialog.dismiss();
                performLoad();
                Toast.makeText(getActivity(), "Unable to remove auth", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAuth() {
        ask(AuthController.authController().removeAllAuth(), new UiAskCallback<Boolean>() {

            private ProgressDialog progressDialog;

            @Override
            public void onPreStart() {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Removing auth...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            public void onCompleted(Boolean res) {
                progressDialog.dismiss();
                performLoad();
            }

            @Override
            public void onError(Throwable t) {
                progressDialog.dismiss();
                performLoad();
                Toast.makeText(getActivity(), "Unable to remove auth", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
