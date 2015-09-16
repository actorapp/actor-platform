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
import java.util.List;

import im.actor.core.api.ApiPublicGroup;
import im.actor.core.api.rpc.RequestGetPublicGroups;
import im.actor.core.api.rpc.ResponseGetPublicGroups;
import im.actor.core.entity.PublicGroup;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.messenger.R;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.fragment.group.view.PublicGroupCardView;
import im.actor.messenger.app.fragment.group.view.PublicGroupSet;
import im.actor.messenger.app.fragment.group.view.PublicGroupSetView;
import im.actor.runtime.files.FileSystemReference;

import static im.actor.messenger.app.core.Core.groups;
import static im.actor.messenger.app.core.Core.messenger;

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

        Command<List<PublicGroup>> cmd = messenger().listPublicGroups();
        if (cmd != null) cmd.start(new CommandCallback<List<PublicGroup>>() {
            @Override
            public void onResult(List<PublicGroup> res) {
                final ArrayList<PublicGroup> groups = new ArrayList<PublicGroup>();
                for (PublicGroup g : res) {
                    groups.add(g);
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
                            if (group.getAvatar() != null && group.getAvatar().getFullImage() != null) {
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


                        PublicGroupSetView topMembersGroupSetView = new PublicGroupSetView(getActivity(), new PublicGroupSet(topByMembersGroupsSet, getString(R.string.join_public_group_top_title), getString(R.string.join_public_group_top_subtitle)), PublicGroupCardView.COUNTER_TYPE_MEMBERS);
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
                                if (group.getAvatar() != null && group.getAvatar().getFullImage() != null) {
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
                            PublicGroupSetView topFriendsGroupSetView = new PublicGroupSetView(getActivity(), new PublicGroupSet(topByFriendsGroupsSet, getString(R.string.join_public_group_top_by_friends_title), getString(R.string.join_public_group_top_by_friends_subtitle)), PublicGroupCardView.COUNTER_TYPE_FRIENDS);
                            topFriendsGroupSetView.setOnGroupClickListener(new PublicGroupSetView.GroupClickListener() {
                                @Override
                                public void onClick(PublicGroup group) {
                                    openGroup(group);
                                }
                            });
                            topMembersGroupSetView.addChain(topFriendsGroupSetView);
                        }

                        PublicGroupSetView allSeparator = new PublicGroupSetView(getActivity(), new PublicGroupSet(null, getString(R.string.join_public_group_all_groups), null), PublicGroupCardView.COUNTER_TYPE_FRIENDS);
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
