package im.actor.sdk.controllers.contacts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.util.KeyboardHelper;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class AddContactActivity extends BaseFragmentActivity {

    private KeyboardHelper helper;
    private EditText searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setTitle(R.string.add_contact_title);

        helper = new KeyboardHelper(this);

        setContentView(R.layout.activity_add);
        findViewById(R.id.container).setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        searchQuery = (EditText) findViewById(R.id.searchField);
        searchQuery.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        searchQuery.setHintTextColor(ActorSDK.sharedActor().style.getTextHintColor());
        findViewById(R.id.dividerTop).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
        findViewById(R.id.dividerBot).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

        ((TextView) findViewById(R.id.cancel)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.ok)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String query = searchQuery.getText().toString();
                if (query.length() == 0) {
                    return;
                }
                execute(messenger().findUsers(query), R.string.progress_common, new CommandCallback<UserVM[]>() {
                    @Override
                    public void onResult(final UserVM[] res) {
                        if (res.length == 0) {
                            new AlertDialog.Builder(AddContactActivity.this)
                                    .setMessage(getString(R.string.alert_invite_text).replace("{0}", query).replace("{appName}", ActorSDK.sharedActor().getAppName()))
                                    .setPositiveButton(R.string.alert_invite_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            String inviteMessage = getString(R.string.invite_message).replace("{inviteUrl}", ActorSDK.sharedActor().getInviteUrl()).replace("{appName}", ActorSDK.sharedActor().getAppName());
                                            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                                            sendIntent.setData(Uri.parse("sms:" + query));
                                            sendIntent.putExtra("sms_body", inviteMessage);
                                            startActivity(sendIntent);
                                            finish();
                                        }
                                    })
                                    .setNegativeButton(R.string.dialog_cancel, null)
                                    .show()
                                    .setCanceledOnTouchOutside(true);
                        } else {
                            execute(messenger().addContact(res[0].getId()), R.string.progress_common, new CommandCallback<Boolean>() {
                                @Override
                                public void onResult(Boolean res2) {
                                    startActivity(Intents.openPrivateDialog(res[0].getId(),
                                            true,
                                            AddContactActivity.this));
                                    finish();
                                }

                                @Override
                                public void onError(Exception e) {
                                    startActivity(Intents.openPrivateDialog(res[0].getId(),
                                            true,
                                            AddContactActivity.this));
                                    finish();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // Never happens
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.setImeVisibility(searchQuery, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        helper.setImeVisibility(searchQuery, false);
    }
}
