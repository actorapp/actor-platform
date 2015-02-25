package im.actor.messenger.app.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.group.GroupInfoFragment;
import im.actor.messenger.app.intents.Intents;

import static im.actor.messenger.core.Core.groups;

/**
 * Created by ex3ndr on 07.10.14.
 */
public class GroupInfoActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int chatId = getIntent().getIntExtra(Intents.EXTRA_GROUP_ID, 0);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (groups().get(chatId) == null) {
            Toast.makeText(this, "Unable to open group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (savedInstanceState == null) {
            showFragment(GroupInfoFragment.create(chatId), false, false);
        }
    }
}
