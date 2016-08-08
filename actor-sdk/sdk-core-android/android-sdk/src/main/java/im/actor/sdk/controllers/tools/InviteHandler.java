package im.actor.sdk.controllers.tools;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.HTTP;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseActivity;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class InviteHandler {

    public static void handleIntent(BaseActivity activity, Intent intent) {
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
                                if (group.has("id") && group.has("isPublic")) {
                                    int gid = group.getInt("id");
                                    boolean isPublic = group.getBoolean("isPublic");
                                    //Check if we have this group
                                    try {
                                        GroupVM groupVM = groups().get(gid);
                                        if (groupVM.isMember().get() || isPublic) {
                                            //Have this group, is member or group is public, just open it
                                            activity.startActivity(Intents.openDialog(Peer.group(gid), false, activity));
                                        } else {
                                            //Have this group, but not member, join it
                                            joinViaToken(token, title, activity);
                                        }
                                    } catch (Exception e) {
                                        //Do not have this group, join it
                                        if (isPublic) {
                                            messenger().findPublicGroupById(gid).then(peer -> activity.startActivity(Intents.openDialog(peer, false, activity)));
                                        } else {
                                            joinViaToken(token, title, activity);
                                        }
                                    }
                                } else {
                                    joinViaToken(token, title, activity);
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

    private static void joinViaToken(String joinGroupUrl, String title, BaseActivity activity) {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setTitle(activity.getString(R.string.invite_link_join_confirm, title))
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        activity.execute(messenger().joinGroupViaToken(joinGroupUrl), R.string.invite_link_title, new CommandCallback<Integer>() {
                            @Override
                            public void onResult(Integer res) {
                                activity.startActivity(Intents.openGroupDialog(res, true, activity));
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
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
