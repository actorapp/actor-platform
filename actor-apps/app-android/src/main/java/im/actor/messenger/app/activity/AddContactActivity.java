package im.actor.messenger.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.view.KeyboardHelper;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by ex3ndr on 03.11.14.
 */
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

        searchQuery = (EditText) findViewById(R.id.searchField);

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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
                                    .setMessage(getString(R.string.alert_invite_text).replace("{0}", query))
                                    .setPositiveButton(R.string.alert_invite_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            String inviteMessage = getString(R.string.invite_message);
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
