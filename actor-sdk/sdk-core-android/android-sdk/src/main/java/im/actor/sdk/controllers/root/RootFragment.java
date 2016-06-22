package im.actor.sdk.controllers.root;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ChatLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.SearchEntity;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.generic.mvvm.DisplayList;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.contacts.AddContactActivity;
import im.actor.sdk.controllers.contacts.ContactsActivity;
import im.actor.sdk.controllers.dialogs.DialogsFragment;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.controllers.fragment.help.HelpActivity;
import im.actor.sdk.controllers.fragment.main.SearchAdapter;
import im.actor.sdk.intents.ShareAction;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.adapters.HeaderViewRecyclerAdapter;
import im.actor.sdk.view.adapters.OnItemClickedListener;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class RootFragment extends BaseFragment implements PeerSelectedCallback {

    //
    // Search
    //
    private MenuItem searchMenu;

    private boolean isSearchVisible = false;
    private RecyclerView searchList;
    private View searchContainer;
    private TextView searchEmptyView;
    private TextView searchHintView;

    private SearchAdapter searchAdapter;
    private BindedDisplayList<SearchEntity> searchDisplay;
    private final DisplayList.Listener searchListener = () -> onSearchChanged();

    //
    // Placeholders
    //
    private View syncInProgressView;
    private View emptyContactsView;

    //
    // Model
    //
    private ShareAction shareAction;


    public RootFragment() {
        setRootFragment(true);
        setUnbindOnPause(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        handleIntent(getActivity().getIntent());

        View res = inflater.inflate(R.layout.activity_root_content, container, false);

        //
        // Search
        //
        searchList = (RecyclerView) res.findViewById(R.id.searchList);
        searchList.setLayoutManager(new ChatLinearLayoutManager(getActivity()));

        searchContainer = res.findViewById(R.id.searchCont);
        searchContainer.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        searchEmptyView = (TextView) res.findViewById(R.id.empty);
        searchHintView = (TextView) res.findViewById(R.id.searchHint);
        searchEmptyView.setTextColor(style.getTextSecondaryColor());
        searchHintView.setTextColor(style.getTextSecondaryColor());
        searchHintView.setVisibility(View.GONE);
        searchEmptyView.setVisibility(View.GONE);

        //
        // Placeholders
        //
        syncInProgressView = res.findViewById(R.id.syncInProgress);
        ((TextView) syncInProgressView.findViewById(R.id.wait_text)).setTextColor(style.getTextSecondaryColor());
        ((TextView) syncInProgressView.findViewById(R.id.sync_text)).setTextColor(style.getMainColor());
        syncInProgressView.findViewById(R.id.sync_background).setBackgroundColor(style.getMainColor());
        syncInProgressView.findViewById(R.id.syncInProgress).setBackgroundColor(style.getMainBackgroundColor());
        emptyContactsView = res.findViewById(R.id.emptyContacts);
        res.findViewById(R.id.emptyContactsFrame).setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        ((TextView) emptyContactsView.findViewById(R.id.no_contacts)).setTextColor(style.getTextSecondaryColor());
        ((TextView) emptyContactsView.findViewById(R.id.no_contacts)).setText(getResources().getString(R.string.main_empty_invite_hint).replace("{appName}", ActorSDK.sharedActor().getAppName()));
        ((TextView) emptyContactsView.findViewById(R.id.add_contact_manually_text)).setTextColor(style.getTextSecondaryColor());
        ((TextView) emptyContactsView.findViewById(R.id.empty_contacts_text)).setTextColor(style.getMainColor());
        emptyContactsView.findViewById(R.id.empty_contacts_bg).setBackgroundColor(style.getMainColor());

        //
        // Placeholder Actions
        //
        TextView addContactBtnText = (TextView) res.findViewById(R.id.addContactButtonText);
        addContactBtnText.setTextColor(style.getTextSecondaryColor());
        addContactBtnText.setTypeface(Fonts.medium());
        TextView inviteBtnText = (TextView) res.findViewById(R.id.inviteButtonText);
        inviteBtnText.setTypeface(Fonts.medium());
        inviteBtnText.setTextColor(style.getTextPrimaryInvColor());

        res.findViewById(R.id.addContactButton).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddContactActivity.class));
        });

        res.findViewById(R.id.inviteButton).setOnClickListener(v -> {
            String inviteMessage = getResources().getString(R.string.invite_message)
                    .replace("{inviteUrl}", ActorSDK.sharedActor().getInviteUrl())
                    .replace("{appName}", ActorSDK.sharedActor().getAppName());
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, inviteMessage);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });

        //
        // Content
        //
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.content, new DialogsFragment())
                    .commit();
        }

        return res;
    }

    public void onHandleIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        bind(messenger().getAppState().getIsAppLoaded(), messenger().getAppState().getIsAppEmpty(), (isAppLoaded, Value, isAppEmpty, Value2) -> {
            Activity activity = getActivity();
            if (isAppEmpty) {
                if (isAppLoaded) {
                    emptyContactsView.setVisibility(View.VISIBLE);
                    syncInProgressView.setVisibility(View.GONE);
                } else {
                    emptyContactsView.setVisibility(View.GONE);
                    syncInProgressView.setVisibility(View.VISIBLE);
                }
            } else {
                emptyContactsView.setVisibility(View.GONE);
                syncInProgressView.setVisibility(View.GONE);
            }
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        // Search Menu
        searchMenu = menu.findItem(R.id.search);
        if (messenger().getAppState().getIsAppEmpty().get()) {
            searchMenu.setVisible(false);
        } else {
            searchMenu.setVisible(true);
        }

        // Search View binding
        SearchView searchView = (SearchView) searchMenu.getActionView();
        searchView.setIconifiedByDefault(true);

        MenuItemCompat.setOnActionExpandListener(searchMenu, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                showSearch();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideSearch();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (isSearchVisible) {
                    if (s.trim().length() > 0) {
                        searchDisplay.initSearch(s.trim().toLowerCase(), false);
                        searchAdapter.setQuery(s.trim().toLowerCase());
                    } else {
                        searchDisplay.initEmpty();
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.help) {
            startActivity(new Intent(getActivity(), HelpActivity.class));
            return true;
        } else if (i == R.id.profile) {
            ActorSDK.sharedActor().startSettingActivity(getActivity());
            return true;
        } else if (i == R.id.contacts) {
            startActivity(new Intent(getActivity(), ContactsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void handleIntent(Intent intent) {
        if (shareAction != null) {
            return;
        }

        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_SEND)) {
                    if ("text/plain".equals(intent.getType())) {
                        shareAction = new ShareAction(intent.getStringExtra(Intent.EXTRA_TEXT));
                    } else if (intent.getParcelableExtra(Intent.EXTRA_STREAM) != null) {
                        ArrayList<String> s = new ArrayList<>();
                        s.add(intent.getParcelableExtra(Intent.EXTRA_STREAM).toString());
                        shareAction = new ShareAction(s);
                    }
                } else if (intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {
                    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    ArrayList<String> s = new ArrayList<>();
                    if (imageUris != null) {
                        for (Uri u : imageUris) {
                            s.add(u.toString());
                        }
                    }
                    if (s.size() > 0) {
                        shareAction = new ShareAction(s);
                    }
                }
            }

            if (intent.getExtras() != null) {
                Bundle extras = intent.getExtras();
                if (extras.containsKey(Intents.EXTRA_SHARE_USER)) {
                    shareAction = new ShareAction(extras.getInt(Intents.EXTRA_SHARE_USER));
                } else if (extras.containsKey(Intents.EXTRA_FORWARD_TEXT)) {
                    shareAction = new ShareAction(
                            extras.getString(Intents.EXTRA_FORWARD_TEXT),
                            extras.getString(Intents.EXTRA_FORWARD_TEXT_RAW));
                } else if (extras.containsKey(Intents.EXTRA_FORWARD_CONTENT)) {
                    shareAction = new ShareAction(extras.getByteArray(Intents.EXTRA_FORWARD_CONTENT));
                }
            }
        }
        if (shareAction == null) {
            setTitle(R.string.app_name);
        } else {
            setTitle(R.string.menu_share);
        }
    }

    //
    // Action
    //

    @Override
    public void onPeerClick(Peer peer) {
        Activity activity = getActivity();
        if (shareAction != null) {
            String name;
            if (peer.getPeerType() == PeerType.PRIVATE) {
                name = messenger().getUser(peer.getPeerId()).getName().get();
            } else if (peer.getPeerType() == PeerType.GROUP) {
                name = messenger().getGroup(peer.getPeerId()).getName().get();
            } else {
                return;
            }
            new AlertDialog.Builder(getActivity())
                    .setMessage(getActivity().getString(R.string.confirm_share) + " " + name + "?")
                    .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {

                        Intent intent = Intents.openDialog(peer, false, activity);

                        if (shareAction.getForwardText() != null) {
                            intent.putExtra(Intents.EXTRA_FORWARD_TEXT, shareAction.getForwardText());
                        }
                        if (shareAction.getForwardTextRaw() != null) {
                            intent.putExtra(Intents.EXTRA_FORWARD_TEXT_RAW, shareAction.getForwardTextRaw());
                        }
                        if (shareAction.getForwardTextRaw() != null) {
                            intent.putExtra(Intents.EXTRA_FORWARD_CONTENT, shareAction.getForwardTextRaw());
                        }

                        if (shareAction.getText() != null) {
                            messenger().sendMessage(peer, shareAction.getText());
                            startActivity(Intents.openDialog(peer, false, activity));
                        } else if (shareAction.getUris().size() > 0) {
                            for (String sendUri : shareAction.getUris()) {
                                executeSilent(messenger().sendUri(peer, Uri.parse(sendUri)));
                            }
                            startActivity(Intents.openDialog(peer, false, activity));
                        } else if (shareAction.getUserId() != null) {
                            String userName = users().get(shareAction.getUserId()).getName().get();
                            String mentionTitle = "@".concat(userName);
                            ArrayList<Integer> mention = new ArrayList<>();
                            mention.add(shareAction.getUserId());
                            messenger().sendMessage(peer, mentionTitle, "[".concat(mentionTitle).concat("](people://".concat(Integer.toString(shareAction.getUserId())).concat(")")), mention);
                        }

                        startActivity(intent);
                        shareAction = null;
                    })
                    .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } else {
            if (activity != null) {
                startActivity(Intents.openDialog(peer, false, activity));
            }
        }
    }

    @Override
    public boolean onPeerLongClick(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            new AlertDialog.Builder(getActivity())
                    .setItems(new CharSequence[]{
                            getString(R.string.dialogs_menu_contact_view),
                            getString(R.string.dialogs_menu_contact_rename),
                            getString(R.string.dialogs_menu_conversation_delete)
                    }, (d, which) -> {
                        if (which == 0) {
                            // View profile
                            ActorSDK.sharedActor().startProfileActivity(getActivity(), peer.getPeerId());
                        } else if (which == 1) {
                            // Rename user
                            startActivity(Intents.editUserName(peer.getPeerId(), getActivity()));
                        } else if (which == 2) {
                            // Delete chat
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(getString(R.string.alert_delete_chat_message, messenger().getUser(peer.getPeerId()).getName().get()))
                                    .setNegativeButton(R.string.dialog_cancel, null)
                                    .setPositiveButton(R.string.alert_delete_chat_yes, (d1, which1) -> {
                                        execute(messenger().deleteChat(peer), R.string.progress_common,
                                                new CommandCallback<Void>() {
                                                    @Override
                                                    public void onResult(Void res) {

                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        Toast.makeText(getActivity(), R.string.toast_unable_delete_chat, Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    })
                                    .show();
                        }
                    })
                    .show();

            return true;
        } else if (peer.getPeerType() == PeerType.GROUP) {
            GroupVM groupVM = groups().get(peer.getPeerId());
            final boolean isMember = groupVM.isMember().get();

            new AlertDialog.Builder(getActivity())
                    .setItems(new CharSequence[]{
                            getString(R.string.dialogs_menu_group_view),
                            getString(R.string.dialogs_menu_group_rename),
                            isMember ? getString(R.string.dialogs_menu_group_leave)
                                    : getString(R.string.dialogs_menu_group_delete),
                    }, (d, which) -> {
                        if (which == 0) {
                            ActorSDK.sharedActor().startGroupInfoActivity(getActivity(), peer.getPeerId());
                        } else if (which == 1) {
                            startActivity(Intents.editGroupTitle(peer.getPeerId(), getActivity()));
                        } else if (which == 2) {
                            if (isMember) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.alert_leave_group_message, groupVM.getName().get()))
                                        .setNegativeButton(R.string.dialog_cancel, null)
                                        .setPositiveButton(R.string.alert_leave_group_yes, (d1, which1) -> {
                                            execute(messenger().leaveGroup(peer.getPeerId()), R.string.progress_common,
                                                    new CommandCallback<Void>() {
                                                        @Override
                                                        public void onResult(Void res) {

                                                        }

                                                        @Override
                                                        public void onError(Exception e) {
                                                            Toast.makeText(getActivity(), R.string.toast_unable_leave, Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.alert_delete_group_title, groupVM.getName().get()))
                                        .setNegativeButton(R.string.dialog_cancel, null)
                                        .setPositiveButton(R.string.alert_delete_group_yes, (d1, which1) -> {
                                            execute(messenger().deleteChat(peer), R.string.progress_common,
                                                    new CommandCallback<Void>() {
                                                        @Override
                                                        public void onResult(Void res) {

                                                        }

                                                        @Override
                                                        public void onError(Exception e) {
                                                            Toast.makeText(getActivity(), R.string.toast_unable_delete_chat, Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        })
                                        .show();
                            }
                        }
                    }).show();
            return true;
        }
        return false;
    }


    //
    // Search
    //

    private void onSearchChanged() {
        if (!searchDisplay.isInSearchState()) {
            showView(searchHintView);
            goneView(searchEmptyView);
        } else {
            goneView(searchHintView);
            if (searchDisplay.getSize() == 0) {
                showView(searchEmptyView);
            } else {
                goneView(searchEmptyView);
            }
        }
    }

    private void showSearch() {
        if (isSearchVisible) {
            return;
        }
        isSearchVisible = true;

        searchDisplay = messenger().buildSearchDisplayList();
        searchAdapter = new SearchAdapter(getActivity(), searchDisplay, new OnItemClickedListener<SearchEntity>() {
            @Override
            public void onClicked(SearchEntity item) {
                startActivity(Intents.openDialog(item.getPeer(), false, getActivity()));
                searchMenu.collapseActionView();
            }

            @Override
            public boolean onLongClicked(SearchEntity item) {
                return false;
            }
        });
        HeaderViewRecyclerAdapter recyclerAdapter = new HeaderViewRecyclerAdapter(searchAdapter);

        View header = new View(getActivity());
        header.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(0)));
        header.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        recyclerAdapter.addHeaderView(header);

        searchList.setAdapter(recyclerAdapter);
        searchDisplay.addListener(searchListener);
        // onHideToolbarCustomView();
        showView(searchHintView, false);
        goneView(searchEmptyView, false);

        showView(searchContainer);
    }

    private void hideSearch() {
        if (!isSearchVisible) {
            return;
        }
        isSearchVisible = false;

        if (searchDisplay != null) {
            searchDisplay.dispose();
            searchDisplay = null;
        }
        searchAdapter = null;
        searchList.setAdapter(null);

        goneView(searchContainer);
        // onShowToolbarCustomView();
        if (searchMenu != null) {
            if (searchMenu.isActionViewExpanded()) {
                searchMenu.collapseActionView();
            }
        }
    }
}
