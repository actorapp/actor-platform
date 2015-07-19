package im.actor.messenger.app.fragment.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.fragment.group.view.PublicGroupCardView;
import im.actor.messenger.app.fragment.group.view.PublicGroupSet;
import im.actor.messenger.app.fragment.group.view.PublicGroupSetView;
import im.actor.messenger.app.util.Screen;
import im.actor.model.api.rpc.RequestGetPublicGroups;
import im.actor.model.api.rpc.ResponseGetPublicGroups;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PublicGroup;
import im.actor.model.files.FileSystemReference;
import im.actor.model.viewmodel.FileVMCallback;
import im.actor.model.viewmodel.GroupVM;

import static im.actor.messenger.app.Core.groups;
import static im.actor.messenger.app.Core.messenger;

/**
 * Created by korka on 30.06.15.
 */
public class JoinPublicGroupFragment extends BaseFragment {

    public static final int MAX_GROUPS_IN_SET = 5;
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
                        ArrayList<PublicGroup> sortedByMembersGroups = new ArrayList<PublicGroup>(groups);
                        Collections.sort(sortedByMembersGroups, new Comparator<PublicGroup>() {
                            @Override
                            public int compare(PublicGroup lhs, PublicGroup rhs) {
                                if (lhs.getMembers() < rhs.getMembers()) {
                                    return 1;
                                } else if (lhs.getMembers() > rhs.getMembers()) {
                                    return -1;
                                }
                                return 0;
                            }
                        });

                        ArrayList<PublicGroup> sortedByFriendsGroups = new ArrayList<PublicGroup>(groups);
                        Collections.sort(sortedByFriendsGroups, new Comparator<PublicGroup>() {
                            @Override
                            public int compare(PublicGroup lhs, PublicGroup rhs) {
                                if (lhs.getFriends() < rhs.getFriends()) {
                                    return 1;
                                } else if (lhs.getMembers() > rhs.getMembers()) {
                                    return -1;
                                }
                                return 0;
                            }
                        });

                        ArrayList<PublicGroup> topByMembersGroupsSet = new ArrayList<PublicGroup>();

                        for (int i = 0; i < MAX_GROUPS_IN_SET; i++) {
                            PublicGroup group = sortedByMembersGroups.get(i);
                            topByMembersGroupsSet.add(group);
                            if (group.getAvatar() != null) {
                                messenger().bindFile(group.getAvatar().getFullImage().getFileReference(), true, new FileVMCallback() {
                                    @Override
                                    public void onNotDownloaded() {
                                    }
                                    @Override
                                    public void onDownloading(float progress) {
                                    }
                                    @Override
                                    public void onDownloaded(FileSystemReference reference) {
                                    }
                                });
                            }
                        }


                        PublicGroupSetView topMembersGroupSetView = new PublicGroupSetView(getActivity(), new PublicGroupSet(topByMembersGroupsSet, getString(R.string.join_public_group_top_title), null), PublicGroupCardView.COUNTER_TYPE_MEMBERS);
                        topMembersGroupSetView.setTitleTopPadding(Screen.dp(15));
                        topMembersGroupSetView.setOnGroupClickListener(new PublicGroupSetView.GroupClickListener() {
                            @Override
                            public void onClick(PublicGroup group) {
                                openGroup(group);
                            }


                        });

                        ArrayList<PublicGroup> topByFriendsGroupsSet = new ArrayList<PublicGroup>();

                        for (int i = 0; i < MAX_GROUPS_IN_SET; i++) {
                            PublicGroup group = sortedByFriendsGroups.get(i);
                            if (group.getFriends() > 0) {
                                topByFriendsGroupsSet.add(group);
                                if (group.getAvatar() != null) {
                                    messenger().bindFile(group.getAvatar().getFullImage().getFileReference(), true, new FileVMCallback() {
                                        @Override
                                        public void onNotDownloaded() {
                                        }

                                        @Override
                                        public void onDownloading(float progress) {
                                        }

                                        @Override
                                        public void onDownloaded(FileSystemReference reference) {
                                        }
                                    });
                                }
                            }
                        }

                        if (topByFriendsGroupsSet.size() > 0) {
                            PublicGroupSetView topFriendsGroupSetView = new PublicGroupSetView(getActivity(), new PublicGroupSet(topByFriendsGroupsSet, getString(R.string.join_public_group_top_by_friends_title), null), PublicGroupCardView.COUNTER_TYPE_FRIENDS);
                            topFriendsGroupSetView.setOnGroupClickListener(new PublicGroupSetView.GroupClickListener() {
                                @Override
                                public void onClick(PublicGroup group) {
                                    openGroup(group);
                                }
                            });
                            topMembersGroupSetView.addChain(topFriendsGroupSetView);
                        }

                        PublicGroupSetView allSeparator = new PublicGroupSetView(getActivity(), new PublicGroupSet(null, getString(R.string.join_public_group_all_groups), null), PublicGroupCardView.COUNTER_TYPE_FRIENDS);
                        allSeparator.setBackgroundColorResource(R.color.bg_main);
                        allSeparator.setTitleColorResource(R.color.action);
                        allSeparator.setPadding(0, Screen.dp(15), 0, Screen.dp(8));
                        topMembersGroupSetView.addChain(allSeparator);
                        listView.addHeaderView(topMembersGroupSetView, null, false);
                        listView.setAdapter(adapter);

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final PublicGroup item = (PublicGroup) parent.getItemAtPosition(position);
                openGroup(item);
            }
        });

        return res;
    }

    private void openGroup(final PublicGroup item) {
        GroupVM groupVM = null;
        try {
            groupVM = groups().get(item.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        joinGroup(item, groupVM != null && groupVM.isMember().get());
    }

    private void joinGroup(final PublicGroup item, boolean isMember) {
        Intent i = new Intent(getActivity(), JoinGroupPopUpActivity.class);
        if (item.getAvatar() != null) i.putExtra("avatar", item.getAvatar().toByteArray());
        i.putExtra("id", item.getId());
        i.putExtra("title", item.getTitle());
        i.putExtra("description", item.getDescription());
        i.putExtra("members", item.getMembers());
        i.putExtra("accessHash", item.getAccessHash());
        i.putExtra("isMember", isMember);

        startActivity(i);
    }
}
