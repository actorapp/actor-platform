package im.actor.sdk.controllers.group;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import im.actor.core.viewmodel.GroupVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.fragment.preview.ViewAvatarActivity;
import im.actor.sdk.controllers.settings.BaseGroupInfoActivity;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class GroupInfoActivity extends BaseFragmentActivity {

    private GroupVM groupInfo;
    private int chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatId = getIntent().getIntExtra(Intents.EXTRA_GROUP_ID, 0);
        groupInfo = groups().get(chatId);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setTitle(null);

        if (savedInstanceState == null) {
            GroupInfoFragment fragment;
            BaseGroupInfoActivity profileIntent = ActorSDK.sharedActor().getDelegate().getGroupInfoIntent(chatId);
            if (profileIntent != null) {
                fragment = profileIntent.getGroupInfoFragment(chatId);
            } else {
                fragment = GroupInfoFragment.create(chatId);
            }

            showFragment(fragment, false, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (groupInfo.isMember().get()) {
            getMenuInflater().inflate(R.menu.group_info, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.leaveGroup) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.alert_leave_group_message).replace("%1$s",
                            groupInfo.getName().get()))
                    .setPositiveButton(R.string.alert_leave_group_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            execute(messenger().leaveGroup(chatId));
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                    .setCanceledOnTouchOutside(true);

            return true;
        } else if (item.getItemId() == R.id.addMember) {
            startActivity(new Intent(this, AddMemberActivity.class)
                    .putExtra("GROUP_ID", chatId));
        } else if (item.getItemId() == R.id.editTitle) {
            startActivity(Intents.editGroupTitle(chatId, this));
        } else if (item.getItemId() == R.id.changePhoto) {
            startActivity(ViewAvatarActivity.viewGroupAvatar(chatId, this));
        }
        return super.onOptionsItemSelected(item);
    }
}
