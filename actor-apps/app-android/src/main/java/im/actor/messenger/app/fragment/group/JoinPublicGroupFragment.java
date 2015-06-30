package im.actor.messenger.app.fragment.group;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.model.api.rpc.RequestGetPublicGroups;
import im.actor.model.api.rpc.ResponseGetPublicGroups;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.GroupMember;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PublicGroup;
import im.actor.model.network.RpcException;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by korka on 30.06.15.
 */
public class JoinPublicGroupFragment extends BaseFragment {

    private ListView listView;
    private JoinPublicGroupAdapter adapter;

    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Command<ResponseGetPublicGroups> cmd = messenger().executeExternalCommand(new RequestGetPublicGroups());
        if (cmd != null) cmd.start(new CommandCallback<ResponseGetPublicGroups>() {
            @Override
            public void onResult(ResponseGetPublicGroups res) {
                final ArrayList<PublicGroup> groups = new ArrayList<PublicGroup>();
                for (im.actor.model.api.PublicGroup g : res.getGroups()) {
                    groups.add(new PublicGroup(g));
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.updateGroups(groups);
                        hideView(emptyView);
                        showView(listView);
                    }
                });

            }

            @Override
            public void onError(Exception e) {
                //oops
            }
        });


        final View res = inflater.inflate(R.layout.fragment_list, container, false);
        listView = (ListView) res.findViewById(R.id.listView);
        emptyView = (TextView) res.findViewById(R.id.emptyView);
        emptyView.setText(getString(R.string.picker_loading));
        adapter = new JoinPublicGroupAdapter(new ArrayList<PublicGroup>(), getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PublicGroup item = (PublicGroup) parent.getItemAtPosition(position);

                execute(messenger().joinPublicGroup(item.getId(), item.getAccessHash()), R.string.main_fab_join_public_group, new CommandCallback<Integer>() {
                    @Override
                    public void onResult(Integer res) {
                        startActivity(Intents.openDialog(Peer.group(res), false, getActivity()));
                    }

                    @Override
                    public void onError(Exception e) {
                        if (e instanceof RpcException) {
                            RpcException re = (RpcException) e;
                            if ("USER_ALREADY_INVITED".equals(re.getTag())) {
                                startActivity(Intents.openDialog(Peer.group(item.getId()), false, getActivity()));
                            }
                        }
                    }
                });

            }
        });

        return res;
    }
}
