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

public class EditAboutFragment extends BaseFragment {

    private KeyboardHelper helper;
    private EditText aboutEdit;
    private TextView hintTv;
    private int type;
    private int id;

    public static EditAboutFragment editAbout(int type, int id) {
        Bundle args = new Bundle();
        args.putInt("EXTRA_TYPE", type);
        args.putInt("EXTRA_ID", id);
        EditAboutFragment res = new EditAboutFragment();
        res.setArguments(args);
        return res;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        type = getArguments().getInt("EXTRA_TYPE");
        id = getArguments().getInt("EXTRA_ID");

        helper = new KeyboardHelper(getActivity());
        View res = inflater.inflate(R.layout.fragment_edit_about, container, false);
        res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        aboutEdit = (EditText) res.findViewById(R.id.nameEdit);
        aboutEdit.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        hintTv = (TextView) res.findViewById(R.id.hint);
        hintTv.setTextColor(ActorSDK.sharedActor().style.getTextHintColor());
        if (type == EditAboutActivity.TYPE_ME) {
            UserVM userModel = users().get(myUid());
            aboutEdit.setText(userModel.getAbout().get());
            aboutEdit.setHint(getString(R.string.edit_about_edittext_hint));
        } else if (type == EditAboutActivity.TYPE_GROUP) {
            GroupVM group = groups().get(id);
            aboutEdit.setText(group.getAbout().get());
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
                    execute(messenger().editGroupAbout(id, about), R.string.edit_about_process, new CommandCallback<Void>() {
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
