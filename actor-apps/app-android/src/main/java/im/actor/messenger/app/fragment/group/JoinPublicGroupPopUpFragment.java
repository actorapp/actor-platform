package im.actor.messenger.app.fragment.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.ViewAvatarActivity;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.model.Messenger;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.droidkit.bser.Bser;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PublicGroup;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.network.RpcException;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.Core.myUid;
import static im.actor.messenger.app.Core.users;

/**
 * Created by korka on 01.07.15.
 */
public class JoinPublicGroupPopUpFragment extends BaseFragment {
    PublicGroup data = new PublicGroup();
    private CoverAvatarView avatarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            Bser.parse(data, getArguments().getByteArray("group"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        View res = inflater.inflate(R.layout.fragment_join_public_group_pop_up, container, false);

        // Avatar
        avatarView = (CoverAvatarView) res.findViewById(R.id.avatar);
        avatarView.setBkgrnd((ImageView) res.findViewById(R.id.avatar_bgrnd));
        avatarView.bind((data.getAvatar() == null ? null : new Avatar(data.getAvatar())));
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ViewAvatarActivity.viewGroupAvatar(data.getId(), getActivity()));
            }
        });
        avatarView.setClickable(false);

        // Title
        ((TextView) res.findViewById(R.id.title)).setText(data.getTitle());

        // Description
        ((TextView) res.findViewById(R.id.description)).setText(data.getDescription());

        // Members count
        final TextView membersCount = (TextView) res.findViewById(R.id.membersCount);
        membersCount.setText(getString(R.string.join_public_group_members_count).concat(Integer.toString(data.getMembersCount())));


        ((TextView) res.findViewById(R.id.joinButtonText)).setTypeface(Fonts.medium());

        res.findViewById(R.id.joinButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                execute(messenger().joinPublicGroup(data.getId(), data.getAccessHash()), R.string.main_fab_join_public_group, new CommandCallback<Integer>() {
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
                                startActivity(Intents.openDialog(Peer.group(data.getId()), false, getActivity()));
                                getActivity().finish();
                            }
                        }
                    }
                });
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
