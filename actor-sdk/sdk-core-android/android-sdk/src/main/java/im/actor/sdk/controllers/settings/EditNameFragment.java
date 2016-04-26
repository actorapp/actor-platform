package im.actor.sdk.controllers.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.messages.Void;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.util.KeyboardHelper;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class EditNameFragment extends BaseFragment {

    private KeyboardHelper helper;
    private EditText nameEdit;
    private TextView hintTv;
    private int type;
    private int id;

    public static EditNameFragment editName(int type, int id) {
        Bundle args = new Bundle();
        args.putInt("EXTRA_TYPE", type);
        args.putInt("EXTRA_ID", id);
        EditNameFragment res = new EditNameFragment();
        res.setArguments(args);
        return res;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        type = getArguments().getInt("EXTRA_TYPE");
        id = getArguments().getInt("EXTRA_ID");

        helper = new KeyboardHelper(getActivity());
        View res = inflater.inflate(R.layout.fragment_edit_name, container, false);
        res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        nameEdit = (EditText) res.findViewById(R.id.nameEdit);
        nameEdit.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        hintTv = (TextView) res.findViewById(R.id.hint);
        hintTv.setTextColor(ActorSDK.sharedActor().style.getTextHintColor());
        if (type == EditNameActivity.TYPE_ME) {
            UserVM userModel = users().get(myUid());
            nameEdit.setText(userModel.getName().get());
        } else if (type == EditNameActivity.TYPE_NICK) {
            UserVM userModel = users().get(myUid());
            nameEdit.setText(userModel.getNick().get());
            nameEdit.setHint(getString(R.string.nickname_edittext_hint));
            hintTv.setText(getString(R.string.nickname_hint).replace("{appName}", ActorSDK.sharedActor().getAppName()));
        } else if (type == EditNameActivity.TYPE_USER) {
            UserVM userModel = users().get(id);
            nameEdit.setText(userModel.getName().get());
        } else if (type == EditNameActivity.TYPE_GROUP) {
            GroupVM group = groups().get(id);
            nameEdit.setText(group.getName().get());
        } else if (type == EditNameActivity.TYPE_GROUP_THEME) {
            GroupVM group = groups().get(id);
            nameEdit.setText(group.getTheme().get());
        }
        res.findViewById(R.id.dividerTop).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
        res.findViewById(R.id.dividerBot).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

        res.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        ((TextView) res.findViewById(R.id.cancel)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        res.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString().trim();
                if (name.length() == 0) {
                    Toast.makeText(getActivity(), R.string.toast_empty_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (type == EditNameActivity.TYPE_ME) {
                    execute(messenger().editMyName(name), R.string.edit_name_process, new CommandCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean res) {
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getActivity(), R.string.toast_unable_change, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (type == EditNameActivity.TYPE_NICK) {
                    execute(messenger().editMyNick(name), R.string.edit_nick_process, new CommandCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean res) {
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getActivity(), R.string.toast_unable_change_nick, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (type == EditNameActivity.TYPE_USER) {
                    execute(messenger().editName(id, name), R.string.edit_name_process, new CommandCallback<Boolean>() {
                        @Override
                        public void onResult(Boolean res) {
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getActivity(), R.string.toast_unable_change, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (type == EditNameActivity.TYPE_GROUP) {
                    execute(messenger().editGroupTitle(id, name), R.string.edit_name_process, new CommandCallback<Void>() {
                        @Override
                        public void onResult(Void res) {
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getActivity(), R.string.toast_unable_change, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (type == EditNameActivity.TYPE_GROUP_THEME) {
                    execute(messenger().editGroupTheme(id, name), R.string.edit_theme_process, new CommandCallback<Void>() {
                        @Override
                        public void onResult(Void res) {
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
        ((TextView) res.findViewById(R.id.ok)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
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
