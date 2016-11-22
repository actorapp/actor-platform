package im.actor.sdk.controllers.conversation.toolbar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.core.entity.GroupType;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.messages.Void;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKLauncher;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.conversation.view.TypingDrawable;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.TintDrawable;
import im.actor.sdk.view.avatar.AvatarView;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ChatToolbarFragment extends BaseFragment {

    public static final int MAX_USERS_FOR_CALLS = 5;
    protected static final int PERMISSIONS_REQUEST_FOR_CALL = 8;
    protected static final int PERMISSIONS_REQUEST_FOR_VIDEO_CALL = 12;

    public static ChatToolbarFragment create(Peer peer) {
        return new ChatToolbarFragment(peer);
    }

    protected Peer peer;

    // Toolbar title root view
    protected View barView;
    // Toolbar unread counter
    protected TextView counter;
    // Toolbar Avatar view
    protected AvatarView barAvatar;
    // Toolbar title view
    protected TextView barTitle;
    // Toolbar subtitle view container
    protected View barSubtitleContainer;
    // Toolbar subtitle text view
    protected TextView barSubtitle;
    // Toolbar typing container
    protected View barTypingContainer;
    // Toolbar typing icon
    protected ImageView barTypingIcon;
    // Toolbar typing text
    protected TextView barTyping;

    public ChatToolbarFragment() {
        setRootFragment(true);
        setUnbindOnPause(true);
    }

    public ChatToolbarFragment(Peer peer) {
        this();
        Bundle args = new Bundle();
        args.putLong("peer", peer.getUnuqueId());
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        peer = Peer.fromUniqueId(getArguments().getLong("peer"));
    }

    @Override
    public void onConfigureActionBar(ActionBar actionBar) {
        super.onConfigureActionBar(actionBar);

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        // Loading Toolbar header views
        ActorStyle style = ActorSDK.sharedActor().style;
        barView = LayoutInflater.from(getActivity()).inflate(R.layout.bar_conversation, null);
        actionBar.setCustomView(barView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        Toolbar parent = (Toolbar) barView.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        barView.findViewById(R.id.home).setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity != null) {
                activity.onBackPressed();
            }
        });

        counter = (TextView) barView.findViewById(R.id.counter);

        counter.setTextColor(style.getDialogsCounterTextColor());
        counter.setBackgroundResource(R.drawable.ic_counter_circle);
        counter.getBackground().setColorFilter(style.getDialogsCounterBackgroundColor(), PorterDuff.Mode.MULTIPLY);
        barTitle = (TextView) barView.findViewById(R.id.title);
        barSubtitleContainer = barView.findViewById(R.id.subtitleContainer);
        barTypingIcon = (ImageView) barView.findViewById(R.id.typingImage);
        barTypingIcon.setImageDrawable(new TypingDrawable());
        barTyping = (TextView) barView.findViewById(R.id.typing);
        barSubtitle = (TextView) barView.findViewById(R.id.subtitle);
        barTypingContainer = barView.findViewById(R.id.typingContainer);
        barTypingContainer.setVisibility(View.INVISIBLE);
        barAvatar = (AvatarView) barView.findViewById(R.id.avatarPreview);
        barAvatar.init(Screen.dp(32), 18);

        barView.findViewById(R.id.titleContainer).setOnClickListener(v -> {
            if (peer.getPeerType() == PeerType.PRIVATE) {
                ActorSDKLauncher.startProfileActivity(getActivity(), peer.getPeerId());
            } else if (peer.getPeerType() == PeerType.GROUP) {
                ActorSDK.sharedActor().startGroupInfoActivity(getActivity(), peer.getPeerId());
            } else {
                // Nothing to do
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing all required Data Binding here

        if (peer.getPeerType() == PeerType.PRIVATE) {

            // Loading user
            UserVM user = users().get(peer.getPeerId());

            // Binding User Avatar to Toolbar
            bind(barAvatar, user.getId(), user.getAvatar(), user.getName());

            // Binding User name to Toolbar
            bind(barTitle, user.getName());
            bind(user.getIsVerified(), (val, valueModel) -> {
                barTitle.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        val ? new TintDrawable(
                                getResources().getDrawable(R.drawable.ic_verified_user_black_18dp),
                                ActorSDK.sharedActor().style.getVerifiedColor()) : null,
                        null);
            });


            // Binding User presence to Toolbar
            bind(barSubtitle, user);

            // Binding User typing to Toolbar
            bindPrivateTyping(barTyping, barTypingContainer, barSubtitle, messenger().getTyping(user.getId()));

            // Refresh menu on contact state change
            bind(user.isContact(), (val, valueModel) -> {
                getActivity().invalidateOptionsMenu();
            });
        } else if (peer.getPeerType() == PeerType.GROUP) {

            // Loading group
            GroupVM group = groups().get(peer.getPeerId());

            // Binding Group avatar to Toolbar
            bind(barAvatar, group.getId(), group.getAvatar(), group.getName());

            // Binding Group title to Toolbar
            bind(barTitle, group.getName());
            if (group.getGroupType() == GroupType.CHANNEL) {
                barTitle.setCompoundDrawablesWithIntrinsicBounds(new TintDrawable(
                        getResources().getDrawable(R.drawable.ic_megaphone_18dp_black),
                        Color.WHITE), null, null, null);
            } else {
                barTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            // Subtitle is always visible for Groups
            barSubtitleContainer.setVisibility(View.VISIBLE);

            // Binding group members
            bind(barSubtitle, barSubtitleContainer, group);

            // Binding group typing
            if (group.getGroupType() == GroupType.GROUP) {
                bindGroupTyping(barTyping, barTypingContainer, barSubtitle, messenger().getGroupTyping(group.getId()));
            }
        }
        
        // Global Counter
        bind(messenger().getGlobalState().getGlobalCounter(), (val, valueModel) -> {
            if (val != null && val > 0) {
                counter.setText(Integer.toString(val));
                showView(counter);
            } else {
                hideView(counter);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflating menu
        menu.clear();
        inflater.inflate(R.menu.chat_menu, menu);

        // Show menu for opening chat contact
//        if (peer.getPeerType() == PeerType.PRIVATE) {
//            menu.findItem(R.id.contact).setVisible(true);
//        } else {
//            menu.findItem(R.id.contact).setVisible(false);
//        }

        // Show menus for leave group and group info view
//        if (peer.getPeerType() == PeerType.GROUP) {
//            GroupVM groupVM = groups().get(peer.getPeerId());
//            if (groupVM.isMember().get()) {
//                menu.findItem(R.id.leaveGroup).setVisible(true);
//                menu.findItem(R.id.groupInfo).setVisible(true);
//            } else {
//                menu.findItem(R.id.leaveGroup).setVisible(false);
//                menu.findItem(R.id.groupInfo).setVisible(false);
//            }
//            if (groupVM.getGroupType() == GroupType.GROUP) {
//                menu.findItem(R.id.clear).setVisible(true);
//            } else {
//                menu.findItem(R.id.clear).setVisible(false);
//            }
//        } else {
//            menu.findItem(R.id.groupInfo).setVisible(false);
//            menu.findItem(R.id.leaveGroup).setVisible(false);
//        }

        // Voice and Video calls
        boolean callsEnabled = ActorSDK.sharedActor().isCallsEnabled();
        boolean videoCallsEnabled = ActorSDK.sharedActor().isVideoCallsEnabled();
        if (callsEnabled) {
            if (peer.getPeerType() == PeerType.PRIVATE) {
                callsEnabled = !users().get(peer.getPeerId()).isBot();
            } else if (peer.getPeerType() == PeerType.GROUP) {

                GroupVM groupVM = groups().get(peer.getPeerId());
                if (groupVM.getGroupType() == GroupType.GROUP && groupVM.isMember().get() && groupVM.getIsCanCall().get()) {
                    callsEnabled = groupVM.getMembersCount().get() <= MAX_USERS_FOR_CALLS;
                    videoCallsEnabled = false;
                } else {
                    callsEnabled = false;
                    videoCallsEnabled = false;
                }
            }
        }
        menu.findItem(R.id.call).setVisible(callsEnabled);
        menu.findItem(R.id.video_call).setVisible(callsEnabled && videoCallsEnabled);

        // Add to contacts
        if (peer.getPeerType() == PeerType.PRIVATE) {
            menu.findItem(R.id.add_to_contacts).setVisible(!users().get(peer.getPeerId()).isContact().get());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            getActivity().finish();
        } /*else if (i == R.id.clear) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.alert_delete_all_messages_text)
                    .setPositiveButton(R.string.alert_delete_all_messages_yes, (dialog, which) -> {
                        execute(messenger().clearChat(peer), R.string.progress_common,
                                new CommandCallback<Void>() {
                                    @Override
                                    public void onResult(Void res) {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(getActivity(), R.string.toast_unable_clear_chat, Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                    .setCanceledOnTouchOutside(true);

        } else if (i == R.id.leaveGroup) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.alert_leave_group_message)
                            .replace("%1$s", groups().get(peer.getPeerId()).getName().get()))
                    .setPositiveButton(R.string.alert_leave_group_yes, (dialog2, which) -> {
                        execute(messenger().leaveGroup(peer.getPeerId()), R.string.progress_common, new CommandCallback<Void>() {
                            @Override
                            public void onResult(Void res) {

                            }

                            @Override
                            public void onError(final Exception e) {
                                Toast.makeText(getActivity(), R.string.toast_unable_leave, Toast.LENGTH_LONG).show();
                            }
                        });
                        getActivity().finish();
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                    .setCanceledOnTouchOutside(true);

        } else if (i == R.id.contact) {
            ActorSDKLauncher.startProfileActivity(getActivity(), peer.getPeerId());
        } else if (i == R.id.groupInfo) {
            ActorSDK.sharedActor().startGroupInfoActivity(getActivity(), peer.getPeerId());
        }*/ else if (i == R.id.add_to_contacts) {
            execute(messenger().addContact(peer.getPeerId()));
        }

        if (ActorSDK.sharedActor().isCallsEnabled()) {
            if (item.getItemId() == R.id.call || item.getItemId() == R.id.video_call) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "call - no permission :c");
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.CAMERA, Manifest.permission.VIBRATE, Manifest.permission.WAKE_LOCK},
                            item.getItemId() == R.id.video_call ? PERMISSIONS_REQUEST_FOR_VIDEO_CALL : PERMISSIONS_REQUEST_FOR_CALL);

                } else {
                    startCall(item.getItemId() == R.id.video_call);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_FOR_CALL || requestCode == PERMISSIONS_REQUEST_FOR_VIDEO_CALL) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCall(requestCode == PERMISSIONS_REQUEST_FOR_VIDEO_CALL);
            }
        }
    }

    private void startCall(boolean video) {
        Command<Long> cmd;
        if (peer.getPeerType() == PeerType.PRIVATE) {
            cmd = video ? messenger().doVideoCall(peer.getPeerId()) : messenger().doCall(peer.getPeerId());
        } else {
            cmd = messenger().doGroupCall(peer.getPeerId());
        }
        execute(cmd, R.string.progress_common);
    }
}
