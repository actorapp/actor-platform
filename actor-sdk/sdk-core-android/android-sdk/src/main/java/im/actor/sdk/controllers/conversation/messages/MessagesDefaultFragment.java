package im.actor.sdk.controllers.conversation.messages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.UnsupportedContent;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.messages.Void;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKLauncher;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.conversation.ChatActivity;
import im.actor.sdk.controllers.share.ShareActivity;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class MessagesDefaultFragment extends MessagesFragment {

    public static MessagesDefaultFragment create(Peer peer) {
        MessagesDefaultFragment res = new MessagesDefaultFragment();
        Bundle bundle = new Bundle();
        bundle.putByteArray("EXTRA_PEER", peer.toByteArray());
        res.setArguments(bundle);
        return res;
    }

    private ActionMode actionMode;

    public MessagesDefaultFragment() {
        super(true);
    }

    @Override
    public void onAvatarClick(int uid) {
        Fragment fragment = getParentFragment();
        if (fragment instanceof MessagesFragmentCallback) {
            ((MessagesFragmentCallback) fragment).onAvatarClick(uid);
        }
    }

    @Override
    public void onAvatarLongClick(int uid) {
        Fragment fragment = getParentFragment();
        if (fragment instanceof MessagesFragmentCallback) {
            ((MessagesFragmentCallback) fragment).onAvatarLongClick(uid);
        }
    }

    @Override
    public boolean onClick(Message message) {
        if (actionMode != null) {
            if (messagesAdapter.isSelected(message)) {
                messagesAdapter.setSelected(message, false);
                if (messagesAdapter.getSelectedCount() == 0) {
                    actionMode.finish();
                    actionMode = null;
                } else {
                    actionMode.invalidate();
                }
            } else {
                messagesAdapter.setSelected(message, true);
                actionMode.invalidate();
            }
            return true;
        } else {
            if (message.getContent() instanceof TextContent && message.getSenderId() == myUid()) {
                Fragment fragment = getParentFragment();
                if (fragment instanceof MessagesFragmentCallback) {
                    ((MessagesFragmentCallback) fragment).onMessageEdit(message.getRid(), ((TextContent) message.getContent()).getText());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onLongClick(final Message message, final boolean hasMyReaction) {
        if (actionMode == null) {
            messagesAdapter.clearSelection();
            messagesAdapter.setSelected(message, true);
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    getActivity().getMenuInflater().inflate(R.menu.messages_context, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    Message[] selected = messagesAdapter.getSelected();
                    if (selected.length > 0) {
                        actionMode.setTitle("" + selected.length);
                    }

                    boolean isAllText = true;

                    for (Message k : selected) {
                        if (!(k.getContent() instanceof TextContent)) {
                            isAllText = false;
                            break;
                        }
                    }

                    menu.findItem(R.id.copy).setVisible(isAllText);
                    menu.findItem(R.id.quote).setVisible(isAllText);
                    menu.findItem(R.id.forward).setVisible(selected.length == 1 || isAllText);
                    menu.findItem(R.id.like).setVisible(selected.length == 1);
                    return false;
                }

                @Override
                public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.delete) {
                        Message[] selected = messagesAdapter.getSelected();
                        final long[] rids = new long[selected.length];
                        for (int i = 0; i < rids.length; i++) {
                            rids[i] = selected[i].getRid();
                        }

                        new AlertDialog.Builder(getActivity())
                                .setMessage(getString(R.string.alert_delete_messages_text)
                                        .replace("{0}", "" + rids.length))
                                .setPositiveButton(R.string.alert_delete_messages_yes,
                                        (dialog, which) -> {
                                            messenger().deleteMessages(peer, rids);
                                            actionMode.finish();
                                        })
                                .setNegativeButton(R.string.dialog_cancel, null)
                                .show()
                                .setCanceledOnTouchOutside(true);
                        return true;
                    } else if (menuItem.getItemId() == R.id.copy) {
                        String text = messenger().getFormatter().formatMessagesExport(messagesAdapter.getSelected());
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Messages", text);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getActivity(), R.string.toast_messages_copied, Toast.LENGTH_SHORT).show();
                        actionMode.finish();
                        return true;
                    } else if (menuItem.getItemId() == R.id.like) {
                        Message currentMessage = messagesAdapter.getSelected()[0];

                        if (hasMyReaction) {
                            ActorSDK.sharedActor().getMessenger().removeReaction(getPeer(), currentMessage.getRid(), "\u2764").start(new CommandCallback<Void>() {
                                @Override
                                public void onResult(Void res) {

                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        } else {
                            ActorSDK.sharedActor().getMessenger().addReaction(getPeer(), currentMessage.getRid(), "\u2764").start(new CommandCallback<Void>() {
                                @Override
                                public void onResult(Void res) {

                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });

                        }
                        actionMode.finish();
                        return true;

                    } else if (menuItem.getItemId() == R.id.quote) {

                        String rawQuote = "";
                        int i = 0;
                        for (Message m : messagesAdapter.getSelected()) {
                            if (m.getContent() instanceof TextContent) {
                                UserVM user = users().get(m.getSenderId());
                                String nick = user.getNick().get();
                                String name = (nick != null && !nick.isEmpty()) ? "@" + nick : user.getName().get();
                                String text = ((TextContent) m.getContent()).getText();
                                rawQuote = rawQuote + name + ": " + text + "\n";
                            }
                        }

                        Fragment fragment = getParentFragment();
                        if (fragment instanceof MessagesFragmentCallback) {
                            ((MessagesFragmentCallback) fragment).onMessageQuote(rawQuote);
                        }

                        actionMode.finish();
                        return true;

                    } else if (menuItem.getItemId() == R.id.forward) {
                        Intent i = new Intent(getActivity(), ShareActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        if (messagesAdapter.getSelected().length == 1) {
                            Message m = messagesAdapter.getSelected()[0];
                            if (m.getContent() instanceof TextContent) {
                                UserVM user = users().get(m.getSenderId());
                                String nick = user.getNick().get();
                                String name = (nick != null && !nick.isEmpty()) ? "@".concat(nick) : user.getName().get();
                                String text = ((TextContent) m.getContent()).getText();
                                String forward = name.concat(": ").concat(text).concat("\n");
                                i.putExtra(Intents.EXTRA_FORWARD_TEXT, forward);
                                i.putExtra(Intents.EXTRA_FORWARD_TEXT_RAW, forward);
                            } else if (!(m.getContent() instanceof UnsupportedContent)) {
                                AbsContent fileMessage = m.getContent();
                                try {
                                    i.putExtra(Intents.EXTRA_FORWARD_CONTENT, AbsContent.serialize(fileMessage));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            String quote = "";
                            String rawQuote = "";
                            int j = 0;
                            for (Message m : messagesAdapter.getSelected()) {
                                if (m.getContent() instanceof TextContent) {
                                    UserVM user = users().get(m.getSenderId());
                                    String nick = user.getNick().get();
                                    String name = (nick != null && !nick.isEmpty()) ? "@".concat(nick) : user.getName().get();
                                    String text = ((TextContent) m.getContent()).getText();
                                    quote = quote.concat(name).concat(": ").concat(text);
                                    rawQuote = rawQuote.concat(name).concat(": ").concat(text).concat("\n");
                                    if (j++ != messagesAdapter.getSelectedCount() - 1) {
                                        quote += ";\n";
                                    } else {
                                        quote += "\n";
                                    }
                                }
                            }
                            i.putExtra(Intents.EXTRA_FORWARD_TEXT, quote);
                            i.putExtra(Intents.EXTRA_FORWARD_TEXT_RAW, rawQuote);
                        }
                        actionMode.finish();
                        startActivity(i);
                        getActivity().finish();
                        return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    MessagesDefaultFragment.this.actionMode = null;
                    messagesAdapter.clearSelection();
                }
            });
        } else {
            if (messagesAdapter.isSelected(message)) {
                messagesAdapter.setSelected(message, false);
                if (messagesAdapter.getSelectedCount() == 0) {
                    actionMode.finish();
                    actionMode = null;
                } else {
                    actionMode.invalidate();
                }
            } else {
                messagesAdapter.setSelected(message, true);
                actionMode.invalidate();
            }
        }
        return true;
    }
}
