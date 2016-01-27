package im.actor.sdk.controllers.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import im.actor.core.entity.Peer;
import im.actor.runtime.Log;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ShortcutActivity extends Activity {
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
        Peer p = Peer.fromUniqueId(i.getLongExtra("peer", 0));
        String text = i.getStringExtra("text");
        if (users().get(p.getPeerId()).isBot()) {
            messenger().sendMessage(p, text);
        }
    }
}
