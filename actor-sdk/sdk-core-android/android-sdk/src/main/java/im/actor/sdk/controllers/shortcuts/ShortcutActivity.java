package im.actor.sdk.controllers.shortcuts;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import im.actor.core.entity.Peer;
import im.actor.runtime.Log;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ShortcutActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {
    String textToSend = "";
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        handeleIntent(i);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handeleIntent(intent);
    }

    private void handeleIntent(Intent i) {
        final Peer p = Peer.fromUniqueId(i.getLongExtra("peer", 0));
        String text = i.getStringExtra("text");
        final String name = users().get(p.getPeerId()).getName().get();

        if (text.contains("{input}") || text.contains("{input/num}")) {
            boolean num = text.contains("{input/num}");
            if (num) {
                text = text.replace("{input/num}", "{input}");
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            String title = text.replace("{input}", "") + " \u2192 " + name;
            builder.setTitle(title);

            LinearLayout ll = new LinearLayout(this);
            ll.setPadding(Screen.dp(20), 0, Screen.dp(20), 0);

            input = new EditText(this);
            if (num) {
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            }
            input.setTextColor(Color.BLACK);
            ll.addView(input, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            builder.setView(ll);

            final String finalText = text;
            builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    send(p, finalText.replace("{input}", input.getText().toString()), name);
                }
            });
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });


            builder.setOnDismissListener(this);
            AlertDialog ad = builder.create();
            ad.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    input.requestFocus();
                    inputMethodManager.showSoftInput(input, 0);
                }
            });
            ad.show();
        } else if (text.contains("{choose}") && text.contains("{/choose}")) {
            final String rawVars = text.substring(text.indexOf("{choose}") + 8, text.indexOf("{/choose}"));
            final String[] vars = rawVars.split("/");
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);

            final String rawCommand = text.replace(rawVars, "").replace("{choose}", "").replace("{/choose}", "");
            builder.setTitle(rawCommand + " \u2192 " + name);

            textToSend = rawCommand + vars[0];

            builder.setSingleChoiceItems(vars, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    textToSend = rawCommand + vars[which];
                }
            });

            builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    send(p, textToSend, name);
                }
            });
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setOnDismissListener(this);
            builder.show();
        } else {
            send(p, text, name);
        }
    }

    private void send(Peer p, String text, String name) {
        if (users().get(p.getPeerId()).isBot()) {
            messenger().sendMessage(p, text);

            Toast.makeText(getApplicationContext(), "\"" + text + "\" sent to " + name + " bot", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        input = new EditText(this);
        input.setVisibility(View.INVISIBLE);
        builder.setView(input);
        AlertDialog adHide = builder.create();
        adHide.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        adHide.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        });
        adHide.show();

    }
}
