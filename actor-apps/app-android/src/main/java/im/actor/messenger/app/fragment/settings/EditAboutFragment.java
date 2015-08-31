package im.actor.messenger.app.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.R;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.view.KeyboardHelper;

import static im.actor.messenger.app.core.Core.groups;
import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.myUid;
import static im.actor.messenger.app.core.Core.users;

public class EditAboutFragment extends BaseFragment {

    public static EditAboutFragment editAbout(int type, int id) {
        Bundle args = new Bundle();
        args.putInt("EXTRA_TYPE", type);
        args.putInt("EXTRA_ID", id);
        EditAboutFragment res = new EditAboutFragment();
        res.setArguments(args);
        return res;
    }

    private KeyboardHelper helper;
    private EditText aboutEdit;

    private int type;
    private int id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        type = getArguments().getInt("EXTRA_TYPE");
        id = getArguments().getInt("EXTRA_ID");

        helper = new KeyboardHelper(getActivity());
        View res = inflater.inflate(R.layout.fragment_edit_about, container, false);
        aboutEdit = (EditText) res.findViewById(R.id.nameEdit);
        if (type == EditAboutActivity.TYPE_ME) {
            UserVM userModel = users().get(myUid());
            aboutEdit.setText(userModel.getAbout().get());
        } else if (type == EditAboutActivity.TYPE_GROUP) {
            GroupVM group = groups().get(id);
            aboutEdit.setText(group.getAbout().get());
        }
        res.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        res.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String about = aboutEdit.getText().toString().trim();
                if (about.length() == 0) {
                    Toast.makeText(getActivity(), R.string.toast_empty_about, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (type == EditAboutActivity.TYPE_ME) {
                    execute(messenger().editMyAbout(about), R.string.progress_common, new CommandCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean res) {
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getActivity(), R.string.toast_unable_change, Toast.LENGTH_SHORT).show();
                        }
                    });
                    //TODO: set group about
                } else if (type == EditAboutActivity.TYPE_GROUP) {
                    execute(messenger().editGroupAbout(id, about), R.string.edit_about_process, new CommandCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean res) {
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getActivity(), R.string.toast_unable_change, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        aboutEdit.requestFocus();
        helper.setImeVisibility(aboutEdit, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        helper.setImeVisibility(aboutEdit, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        helper = null;
    }
}
