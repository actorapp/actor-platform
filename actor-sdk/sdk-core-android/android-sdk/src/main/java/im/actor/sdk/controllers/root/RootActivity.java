package im.actor.sdk.controllers.root;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.HTTP;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

/**
 * Root Activity of Application
 */
public class RootActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        //
        // Configure Toolbar
        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        setSupportActionBar(toolbar);
        if (ActorSDK.sharedActor().style.getToolBarColor() != 0) {
            toolbar.setBackgroundDrawable(new ColorDrawable(ActorSDK.sharedActor().style.getToolBarColor()));
        }

        if (savedInstanceState == null) {
            Fragment fragment = ActorSDK.sharedActor().getDelegate().fragmentForRoot();
            if (fragment == null) {
                fragment = new RootFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.root, fragment)
                    .commit();
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW) && intent.getData() != null) {
            String joinGroupUrl = intent.getData().toString();
            if (joinGroupUrl != null && (joinGroupUrl.contains("join") || joinGroupUrl.contains("token"))) {
                String[] urlSplit = null;
                if (joinGroupUrl.contains("join")) {
                    urlSplit = joinGroupUrl.split("/join/");
                } else if (joinGroupUrl.contains("token")) {
                    urlSplit = joinGroupUrl.split("token=");
                }
                if (urlSplit != null) {
                    joinGroupUrl = urlSplit[urlSplit.length - 1];

                    final String token = joinGroupUrl;
                    HTTP.getMethod(ActorSDK.sharedActor().getInviteDataUrl() + joinGroupUrl, 0, 0, 0).then(new Consumer<HTTPResponse>() {
                        @Override
                        public void apply(HTTPResponse httpResponse) {
                            try {
                                JSONObject data = new JSONObject(new String(httpResponse.getContent(), "UTF-8"));
                                JSONObject group = data.getJSONObject("group");
                                String title = group.getString("title");
                                if (group.has("id")) {
                                    int gid = group.getInt("id");
                                    //Check if we have this group
                                    try {
                                        GroupVM groupVM = groups().get(gid);
                                        if (groupVM.isMember().get()) {
                                            //Have this group, is member, just open it
                                            startActivity(Intents.openDialog(Peer.group(gid), false, RootActivity.this));
                                        } else {
                                            //Have this group, but not member, join it
                                            joinViaToken(token, title);
                                        }
                                    } catch (Exception e) {
                                        //Do not have this group, join it
                                        joinViaToken(token, title);
                                    }
                                } else {
                                    joinViaToken(token, title);
                                }
                            } catch (JSONException | UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }

    private void joinViaToken(String joinGroupUrl, String title) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.invite_link_join_confirm, title))
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        execute(messenger().joinGroupViaToken(joinGroupUrl), R.string.invite_link_title, new CommandCallback<Integer>() {
                            @Override
                            public void onResult(Integer res) {
                                startActivity(Intents.openGroupDialog(res, true, RootActivity.this));
                                finish();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(RootActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }
}
