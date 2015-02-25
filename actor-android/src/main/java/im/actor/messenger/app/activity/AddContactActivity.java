package im.actor.messenger.app.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.view.KeyboardHelper;

/**
 * Created by ex3ndr on 03.11.14.
 */
public class AddContactActivity extends BaseFragmentActivity {

    private KeyboardHelper helper;
    private EditText searchQuery;
    private View progress;

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
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

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

//                ask(ContactsActor.contactsList().findUsers(query), new UiAskCallback<UserModel[]>() {
//                    @Override
//                    public void onPreStart() {
//                        showView(progress);
//                    }
//
//                    @Override
//                    public void onCompleted(final UserModel[] res) {
//                        goneView(progress);
//                        if (res.length == 0) {
//                            new AlertDialog.Builder(AddContactActivity.this)
//                                    .setMessage(getString(R.string.alert_invite_text).replace("{0}", query))
//                                    .setPositiveButton(R.string.alert_invite_yes, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            String inviteMessage = getString(R.string.invite_message);
//                                            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
//                                            sendIntent.setData(Uri.parse("sms:" + query));
//                                            sendIntent.putExtra("sms_body", inviteMessage);
//                                            startActivity(sendIntent);
//                                            finish();
//                                        }
//                                    })
//                                    .setNegativeButton(R.string.dialog_cancel, null)
//                                    .show()
//                                    .setCanceledOnTouchOutside(true);
//                        } else {
////                            ask(ContactsActor.contactsList().addContact(res[0].getId()), new UiAskCallback<Boolean>() {
////                                @Override
////                                public void onPreStart() {
////
////                                }
////
////                                @Override
////                                public void onCompleted(Boolean res2) {
////                                    startActivity(Intents.openPrivateDialog(res[0].getId(),
////                                            true,
////                                            AddContactActivity.this));
////                                    finish();
////                                }
////
////                                @Override
////                                public void onError(Throwable t) {
////                                    startActivity(Intents.openPrivateDialog(res[0].getId(),
////                                            true,
////                                            AddContactActivity.this));
////                                    finish();
////                                }
////                            });
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        // Just ignore result
//                        t.printStackTrace();
//                        goneView(progress);
//                    }
//                });
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
