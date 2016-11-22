package im.actor.sdk.controllers.group;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.jetbrains.annotations.Nullable;

import im.actor.core.util.JavaUtil;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.tools.MediaPickerCallback;
import im.actor.sdk.controllers.tools.MediaPickerFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class GroupEditFragment extends BaseFragment implements MediaPickerCallback {

    public static GroupEditFragment create(int groupId) {
        Bundle bundle = new Bundle();
        bundle.putInt("groupId", groupId);
        GroupEditFragment editFragment = new GroupEditFragment();
        editFragment.setArguments(bundle);
        return editFragment;
    }

    private int groupId;
    private GroupVM groupVM;
    private AvatarView avatarView;
    private MaterialEditText titleEditText;
    private EditText descriptionEditText;

    public GroupEditFragment() {
        setTitle(R.string.group_title);
        setHomeAsUp(true);
        setShowHome(true);
        setRootFragment(true);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        groupId = getArguments().getInt("groupId");
        groupVM = messenger().getGroup(groupId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_edit_info, container, false);
        res.findViewById(R.id.rootContainer).setBackgroundColor(style.getBackyardBackgroundColor());
        res.findViewById(R.id.topContainer).setBackgroundColor(style.getMainBackgroundColor());

        avatarView = (AvatarView) res.findViewById(R.id.avatar);
        avatarView.init(Screen.dp(52), 24);
        avatarView.bind(groupVM.getAvatar().get(), groupVM.getName().get(), groupId);
        avatarView.setOnClickListener(v -> {
            onAvatarClicked();
        });

        titleEditText = (MaterialEditText) res.findViewById(R.id.name);
        titleEditText.setTextColor(style.getTextPrimaryColor());
        titleEditText.setBaseColor(style.getAccentColor());
        titleEditText.setMetHintTextColor(style.getTextHintColor());
        titleEditText.setText(groupVM.getName().get());
        titleEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                avatarView.updatePlaceholder(editable.toString(), groupId);
            }
        });

        descriptionEditText = (EditText) res.findViewById(R.id.description);
        descriptionEditText.setTextColor(style.getTextPrimaryColor());
        descriptionEditText.setHintTextColor(style.getTextHintColor());
        descriptionEditText.setText(groupVM.getAbout().get());

        // Media Picker
        getChildFragmentManager().beginTransaction()
                .add(new MediaPickerFragment(), "picker")
                .commitNow();

        return res;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.next, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.next) {

            Promise<Void> res = Promise.success(null);
            boolean isPerformed = false;

            if (titleEditText != null) {
                String title = titleEditText.getText().toString().trim();
                if (title.length() > 0) {
                    if (!title.equals(messenger().getGroup(groupId).getName().get())) {
                        res = res.chain(r -> messenger().editGroupTitle(groupId, title));
                        isPerformed = true;
                    }
                } else {
                    return true;
                }
            }

            if (descriptionEditText != null) {
                String description = descriptionEditText.getText().toString().trim();
                if (description.length() == 0) {
                    description = null;
                }

                if (!JavaUtil.equalsE(description, groupVM.getAbout().get())) {
                    final String finalDescription = description;
                    res = res.chain(r -> messenger().editGroupAbout(groupId, finalDescription));
                    isPerformed = true;
                }
            }

            if (isPerformed) {
                execute(res).then(r -> {
                    finishActivity();
                }).failure(r -> {
                    Toast.makeText(getActivity(), R.string.toast_unable_change, Toast.LENGTH_SHORT).show();
                });
            } else {
                finishActivity();
            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void onAvatarClicked() {
        CharSequence[] args;
        if (groupVM.getAvatar().get() != null) {
            args = new CharSequence[]{getString(R.string.pick_photo_camera),
                    getString(R.string.pick_photo_gallery),
                    getString(R.string.pick_photo_remove)};
        } else {
            args = new CharSequence[]{getString(R.string.pick_photo_camera),
                    getString(R.string.pick_photo_gallery)};
        }
        new AlertDialog.Builder(getActivity()).setItems(args, (d, which) -> {
            if (which == 0) {
                findPicker().requestPhoto(true);
            } else if (which == 1) {
                findPicker().requestGallery(true);
            } else if (which == 2) {
                messenger().removeGroupAvatar(groupId);
                avatarView.bind(null, groupVM.getName().get(), groupId);
            }
        }).show();
    }

    @Override
    public void onPhotoCropped(String path) {
        messenger().changeGroupAvatar(groupId, path);
        avatarView.bindRaw(path);
    }

    private MediaPickerFragment findPicker() {
        return (MediaPickerFragment) getChildFragmentManager().findFragmentByTag("picker");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        titleEditText = null;
        descriptionEditText = null;
        if (avatarView != null) {
            avatarView.unbind();
            avatarView = null;
        }
    }
}
