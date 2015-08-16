package im.actor.messenger.app.fragment.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.Peer;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.fragment.preview.ViewAvatarActivity;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.runtime.bser.Bser;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by korka on 01.07.15.
 */
public class JoinPublicGroupPopUpFragment extends BaseFragment {
    Avatar avatar = new Avatar();
    int id;
    long accessHash;
    String description;
    String title;
    int members;
    boolean isMember;
    private CoverAvatarView avatarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            if (getArguments().getByteArray("avatar") != null)
                Bser.parse(avatar, getArguments().getByteArray("avatar"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        id = getArguments().getInt("id");
        title = getArguments().getString("title");
        description = getArguments().getString("description");
        accessHash = getArguments().getLong("accessHash");
        members = getArguments().getInt("members");
        isMember = getArguments().getBoolean("isMember");
        View res = inflater.inflate(R.layout.fragment_join_public_group_pop_up, container, false);

        // Avatar
        avatarView = (CoverAvatarView) res.findViewById(R.id.avatar);
        avatarView.setBkgrnd((ImageView) res.findViewById(R.id.avatar_bgrnd));
        avatarView.bind(avatar);
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ViewAvatarActivity.viewGroupAvatar(id, getActivity()));
            }
        });
        avatarView.setClickable(false);

        // Title
        ((TextView) res.findViewById(R.id.title)).setText(title);

        // Description
        ((TextView) res.findViewById(R.id.description)).setText(description);

        // Members count
        final TextView membersCount = (TextView) res.findViewById(R.id.membersCount);
        membersCount.setText(getString(R.string.join_public_group_members_count).concat(Integer.toString(members)));

        TextView buttonText = (TextView) res.findViewById(R.id.joinButtonText);
        buttonText.setTypeface(Fonts.medium());
        buttonText.setText(getString(isMember ? R.string.open : R.string.join));

        res.findViewById(R.id.joinButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMember) {
                    startActivity(Intents.openDialog(Peer.group(id), false, getActivity()));
                    getActivity().finish();
                } else {
                    execute(messenger().joinPublicGroup(id, accessHash), R.string.main_fab_join_public_group, new CommandCallback<Integer>() {
                        @Override
                        public void onResult(Integer res) {
                            startActivity(Intents.openDialog(Peer.group(res), false, getActivity()));
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Exception e) {
                            if (e instanceof RpcException) {
                                RpcException re = (RpcException) e;
                                if ("USER_ALREADY_INVITED".equals(re.getTag())) {
                                    startActivity(Intents.openDialog(Peer.group(id), false, getActivity()));
                                    getActivity().finish();
                                }
                            }
                        }
                    });
                }
            }
        });

        return res;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (avatarView != null) {
            avatarView.unbind();
            avatarView = null;
        }
    }
}
