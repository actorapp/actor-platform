package im.actor.messenger.app.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.EditNameActivity;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.view.KeyboardHelper;
import im.actor.messenger.core.actors.base.UiAskCallback;
import im.actor.messenger.core.actors.groups.GroupsActor;
import im.actor.messenger.core.actors.profile.EditNameActor;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.UserModel;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class EditNameFragment extends BaseCompatFragment {


    public static EditNameFragment editName(int type, int id) {
        Bundle args = new Bundle();
        args.putInt("EXTRA_TYPE", type);
        args.putInt("EXTRA_ID", id);
        EditNameFragment res = new EditNameFragment();
        res.setArguments(args);
        return res;
    }

    private KeyboardHelper helper;
    private EditText nameEdit;

    private int type;
    private int id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        type = getArguments().getInt("EXTRA_TYPE");
        id = getArguments().getInt("EXTRA_ID");

        helper = new KeyboardHelper(getActivity());
        View res = inflater.inflate(R.layout.fragment_edit_name, container, false);
        nameEdit = (EditText) res.findViewById(R.id.nameEdit);
        if (type == EditNameActivity.TYPE_ME) {
            UserModel userModel = users().get(myUid());
            nameEdit.setText(userModel.getName());
        } else if (type == EditNameActivity.TYPE_USER) {
            UserModel userModel = users().get(id);
            nameEdit.setText(userModel.getName());
        } else if (type == EditNameActivity.TYPE_GROUP) {
            GroupModel info = groups().get(id);
            nameEdit.setText(info.getTitle());
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
                String name = nameEdit.getText().toString().trim();
                if (name.length() == 0) {
                    Toast.makeText(getActivity(), R.string.toast_empty_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (type == EditNameActivity.TYPE_ME) {
                    ask(EditNameActor.editName().editMyName(name), getString(R.string.edit_name_process), new UiAskCallback<Boolean>() {
                        @Override
                        public void onPreStart() {

                        }

                        @Override
                        public void onCompleted(Boolean res) {
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Throwable t) {
                            Toast.makeText(getActivity(), R.string.toast_unable_change, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (type == EditNameActivity.TYPE_GROUP) {
                    ask(GroupsActor.groupUpdates().editGroupName(id, name), getString(R.string.edit_name_process), new UiAskCallback<Boolean>() {

                        @Override
                        public void onPreStart() {
                        }

                        @Override
                        public void onCompleted(Boolean res) {
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Throwable t) {
                            Toast.makeText(getActivity(), R.string.toast_unable_change, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (type == EditNameActivity.TYPE_USER) {
                    ask(EditNameActor.editName().editName(id, name), getString(R.string.edit_name_process), new UiAskCallback<Boolean>() {
                        @Override
                        public void onPreStart() {

                        }

                        @Override
                        public void onCompleted(Boolean res) {
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Throwable t) {
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
        nameEdit.requestFocus();
        helper.setImeVisibility(nameEdit, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        helper.setImeVisibility(nameEdit, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        helper = null;
    }
}
