package im.actor.messenger.app.fragment.compose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.KeyboardHelper;

/**
 * Created by ex3ndr on 04.10.14.
 */
public class GroupNameFragment extends BaseFragment {

    private static final int REQUEST_AVATAR = 1;

    private EditText groupName;
    private AvatarView avatarView;

    private String avatarPath;

    private KeyboardHelper helper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        helper = new KeyboardHelper(getActivity());

        View res = inflater.inflate(R.layout.fragment_create_group_name, container, false);
        groupName = (EditText) res.findViewById(R.id.groupTitle);
        groupName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    next();
                    return true;
                }
                return false;
            }
        });

        avatarView = (AvatarView) res.findViewById(R.id.avatar);
        avatarView.init(Screen.dp(96), 24);
        avatarView.getHierarchy().setPlaceholderImage(R.drawable.circle_placeholder);
        // avatarView.getHierarchy().setControllerOverlay(getResources().getDrawable(R.drawable.circle_selector));
        avatarView.setImageURI(null);

        res.findViewById(R.id.pickAvatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Intents.pickAvatar(avatarPath != null, getActivity()), REQUEST_AVATAR);
            }
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
            ((CreateGroupActivity) getActivity()).showNextFragment(
                    GroupUsersFragment.create(groupName.getText().toString().trim(), avatarPath), false, true);
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
