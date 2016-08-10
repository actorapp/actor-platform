package im.actor.sdk.controllers.share;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.content.AbsContent;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.dialogs.DialogsFragment;
import im.actor.sdk.controllers.dialogs.DialogsFragmentDelegate;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.placeholder.GlobalPlaceholderFragment;
import im.actor.sdk.controllers.search.GlobalSearchDelegate;
import im.actor.sdk.controllers.search.GlobalSearchFragment;
import im.actor.sdk.intents.ShareAction;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ShareFragment extends BaseFragment implements DialogsFragmentDelegate, GlobalSearchDelegate {

    public static final String ARG_INTENT_ACTION = "intent_action";
    public static final String ARG_INTENT_TYPE = "intent_type";

    private ShareAction shareAction;

    public ShareFragment() {
        setRootFragment(true);
        setUnbindOnPause(true);
        setTitle(R.string.menu_share);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.activity_root_content, container, false);

        Bundle args = getArguments();
        String action = args.getString(ARG_INTENT_ACTION);
        if (action != null) {
            String type = args.getString(ARG_INTENT_TYPE);
            if (action.equals(Intent.ACTION_SEND)) {
                if ("text/plain".equals(type)) {
                    shareAction = new ShareAction(args.getString(Intent.EXTRA_TEXT));
                } else if (args.getParcelable(Intent.EXTRA_STREAM) != null) {
                    ArrayList<String> s = new ArrayList<>();
                    s.add(args.getParcelable(Intent.EXTRA_STREAM).toString());
                    shareAction = new ShareAction(s);
                } else {
                    // Unable to load
                    getActivity().finish();
                }
            } else if (action.equals(Intent.ACTION_SEND_MULTIPLE)) {
                ArrayList<Uri> imageUris = args.getParcelableArrayList(Intent.EXTRA_STREAM);
                ArrayList<String> s = new ArrayList<>();
                if (imageUris != null) {
                    for (Uri u : imageUris) {
                        s.add(u.toString());
                    }
                }
                if (s.size() > 0) {
                    shareAction = new ShareAction(s);
                } else {
                    // Unable to load
                    getActivity().finish();
                }
            } else {
                // Unable to load
                getActivity().finish();
            }
        } else {
            if (args.containsKey(Intents.EXTRA_SHARE_USER)) {
                shareAction = new ShareAction(args.getInt(Intents.EXTRA_SHARE_USER));
            } else if (args.containsKey(Intents.EXTRA_FORWARD_TEXT)) {
                shareAction = new ShareAction(
                        args.getString(Intents.EXTRA_FORWARD_TEXT),
                        args.getString(Intents.EXTRA_FORWARD_TEXT_RAW));
            } else if (args.containsKey(Intents.EXTRA_FORWARD_CONTENT)) {
                shareAction = new ShareAction(args.getByteArray(Intents.EXTRA_FORWARD_CONTENT));
            } else {
                // Unable to load
                getActivity().finish();
            }
        }

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.content, new DialogsFragment())
                    .add(R.id.search, new GlobalSearchFragment())
                    .add(R.id.placeholder, new GlobalPlaceholderFragment())
                    .commit();
        }

        return res;
    }

    @Override
    public void onPeerClicked(Peer peer) {
        Activity activity = getActivity();
        String name;
        if (peer.getPeerType() == PeerType.PRIVATE) {
            name = messenger().getUser(peer.getPeerId()).getName().get();
        } else if (peer.getPeerType() == PeerType.GROUP) {
            name = messenger().getGroup(peer.getPeerId()).getName().get();
        } else {
            activity.finish();
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
                    } else if (shareAction.getUris().size() > 0) {
                        for (String sendUri : shareAction.getUris()) {
                            executeSilent(messenger().sendUri(peer, Uri.parse(sendUri), ActorSDK.sharedActor().getAppName()));
                        }
                    } else if (shareAction.getUserId() != null) {
                        String userName = users().get(shareAction.getUserId()).getName().get();
                        String mentionTitle = "@".concat(userName);
                        ArrayList<Integer> mention = new ArrayList<>();
                        mention.add(shareAction.getUserId());
                        messenger().sendMessage(peer, mentionTitle, "[".concat(mentionTitle).concat("](people://".concat(Integer.toString(shareAction.getUserId())).concat(")")), mention);
                    } else if (shareAction.getDocContent() != null) {
                        try {
                            messenger().forwardContent(peer, AbsContent.parse(shareAction.getDocContent()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    startActivity(intent);
                    activity.finish();
                    shareAction = null;
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public boolean onPeerLongClicked(Peer peer) {
        return false;
    }
}
