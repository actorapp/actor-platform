package im.actor.sdk.controllers.settings;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.User;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Consumer;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.fragment.BaseFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class BlockedListFragment extends BaseFragment {

    RecyclerView list;
    TextView emptyView;
    BlockedAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_recycler_list, container, false);

        list = (RecyclerView) res.findViewById(R.id.listView);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        emptyView = (TextView) res.findViewById(R.id.emptyView);
        emptyView.setText(R.string.blocked_loading);

        res.setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
        emptyView.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());

        adapter = new BlockedAdapter(new ArrayList<User>(), new BlockedAdapter.OnBlockedClickListener() {
            @Override
            public void onClick(UserVM u) {
                execute(messenger().unblockUser(u.getId())
                        .then(new Consumer<Void>() {
                            @Override
                            public void apply(Void aVoid) {
                                checkBlockedList();
                            }
                        }));
            }
        });
        list.setAdapter(adapter);
        checkBlockedList();
        return res;
    }

    public void checkBlockedList() {
        messenger().loadBlockedUsers()
                .then(new Consumer<List<User>>() {
                    @Override
                    public void apply(final List<User> users) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (users.size() > 0) {
                                    hideView(emptyView);
                                    showView(list);
                                } else {
                                    hideView(list);
                                    showView(emptyView);
                                    emptyView.setText(R.string.blocked_empty_list);
                                }
                                adapter.setBlockedList(users);
                            }
                        });

                    }
                });
    }

}
