package im.actor.sdk.controllers.compose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.group.GroupTypeFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.util.KeyboardHelper;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class GroupNameFragment extends BaseFragment {

    private static final int REQUEST_AVATAR = 1;

    private boolean isChannel;

    private EditText groupName;
    private AvatarView avatarView;

    private String avatarPath;

    private KeyboardHelper helper;

    public GroupNameFragment() {
        setRootFragment(true);
        setHomeAsUp(true);
    }

    public GroupNameFragment(boolean isChannel) {
        this();
        Bundle args = new Bundle();
        args.putBoolean("isChannel", isChannel);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        this.isChannel = getArguments().getBoolean("isChannel");

        if (isChannel) {
            setTitle(R.string.create_channel_title);
        } else {
            setTitle(R.string.create_group_title);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        helper = new KeyboardHelper(getActivity());

        View res = inflater.inflate(R.layout.fragment_create_group_name, container, false);
        res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        TextView hintTextView = (TextView) res.findViewById(R.id.create_group_hint);
        hintTextView.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        if (isChannel) {
            hintTextView.setText(R.string.create_channel_hint);
        } else {
            hintTextView.setText(R.string.create_group_hint);
        }

        groupName = (EditText) res.findViewById(R.id.groupTitle);
        groupName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                next();
                return true;
            }
            return false;
        });
        if (isChannel) {
            groupName.setHint(R.string.create_channel_name_hint);
        } else {
            groupName.setHint(R.string.create_group_name_hint);
        }
        groupName.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        groupName.setHintTextColor(ActorSDK.sharedActor().style.getTextHintColor());

        avatarView = (AvatarView) res.findViewById(R.id.avatar);
        avatarView.init(Screen.dp(96), 24);
        avatarView.getHierarchy().setPlaceholderImage(R.drawable.circle_placeholder);
        // avatarView.getHierarchy().setControllerOverlay(getResources().getDrawable(R.drawable.circle_selector));
        avatarView.setImageURI(null);

        res.findViewById(R.id.pickAvatar).setOnClickListener(view -> {
            startActivityForResult(Intents.pickAvatar(avatarPath != null, getActivity()), REQUEST_AVATAR);
        });

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        groupName.requestFocus();
        helper.setImeVisibility(groupName, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.next, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.next) {
            next();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void next() {
        String title = groupName.getText().toString().trim();
        if (title.length() > 0) {
            if (isChannel) {
                execute(messenger().createChannel(groupName.getText().toString().trim(), avatarPath).then(gid -> {
                    ((CreateGroupActivity) getActivity()).showNextFragment(
                            GroupTypeFragment.create(gid, true), false);
                }));
            } else {
                ((CreateGroupActivity) getActivity()).showNextFragment(
                        GroupUsersFragment.createGroup(groupName.getText().toString().trim(), avatarPath), false);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AVATAR && resultCode == Activity.RESULT_OK) {
            int res = data.getIntExtra(Intents.EXTRA_RESULT, Intents.RESULT_IMAGE);
            if (res == Intents.RESULT_DELETE) {
                this.avatarPath = null;
                this.avatarView.unbind();
            } else if (res == Intents.RESULT_IMAGE) {
                this.avatarPath = data.getStringExtra(Intents.EXTRA_IMAGE);
                this.avatarView.bindRaw(avatarPath);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        helper.setImeVisibility(groupName, false);
    }
}
