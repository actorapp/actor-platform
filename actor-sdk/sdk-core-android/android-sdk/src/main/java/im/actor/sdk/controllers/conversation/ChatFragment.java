package im.actor.sdk.controllers.conversation;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import im.actor.core.entity.Peer;
import im.actor.core.entity.Sticker;
import im.actor.core.network.RpcException;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.conversation.attach.AbsAttachFragment;
import im.actor.sdk.controllers.conversation.inputbar.InputBarCallback;
import im.actor.sdk.controllers.conversation.inputbar.InputBarFragment;
import im.actor.sdk.controllers.conversation.messages.MessagesDefaultFragment;
import im.actor.sdk.controllers.conversation.messages.MessagesFragmentCallback;
import im.actor.sdk.controllers.conversation.quote.QuoteCallback;
import im.actor.sdk.controllers.conversation.quote.QuoteFragment;
import im.actor.sdk.controllers.conversation.attach.AttachFragment;
import im.actor.sdk.controllers.conversation.toolbar.ChatToolbarFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class ChatFragment extends BaseFragment implements InputBarCallback, MessagesFragmentCallback, QuoteCallback {

    public static ChatFragment create(Peer peer) {
        ChatFragment res = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("peer", peer.getUnuqueId());
        res.setArguments(bundle);
        return res;
    }

    private View quoteContainer;
    private Peer peer;
    private long editRid;
    private String currentQuote;

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        peer = Peer.fromUniqueId(getArguments().getLong("peer"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_chat, container, false);

        quoteContainer = res.findViewById(R.id.quoteFragment);

        if (savedInstanceState == null) {
            Fragment toolbarFragment = ActorSDK.sharedActor().getDelegate().fragmentForToolbar(peer);
            if (toolbarFragment == null) {
                toolbarFragment = new ChatToolbarFragment(peer);
            }
            getChildFragmentManager().beginTransaction()
                    .add(toolbarFragment, "toolbar")
                    .add(R.id.messagesFragment, MessagesDefaultFragment.create(peer))
                    .add(R.id.sendFragment, new InputBarFragment())
                    .add(R.id.quoteFragment, new QuoteFragment())
                    .commitNow();

            AbsAttachFragment fragment = ActorSDK.sharedActor().getDelegate().fragmentForAttachMenu(peer);
            if (fragment == null) {
                fragment = new AttachFragment(peer);
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.overlay, fragment)
                    .commit();
        }

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        findInputBar().setText(messenger().loadDraft(peer), true);
    }

    public boolean onBackPressed() {
        AbsAttachFragment attachFragment = findShareFragment();
        if (attachFragment != null) {
            if (attachFragment.onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTextFocusChanged(boolean isFocused) {

    }

    @Override
    public void onTyping() {
        messenger().onTyping(peer);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void onTextSent(String sequence) {
        if (sequence.trim().length() > 0) {
            if (editRid == 0) {
                if (currentQuote != null) {
                    messenger().sendMessage(peer, currentQuote + sequence);
                    currentQuote = null;
                    hideQuote();
                } else {
                    messenger().sendMessage(peer, sequence);
                }
            } else {
                messenger().updateMessage(peer, sequence, editRid).failure(e -> {
                    Activity activity = getActivity();
                    if (activity != null) {
                        RpcException re = (RpcException) e;
                        String error;
                        if (re.getTag().equals("NOT_IN_TIME_WINDOW")) {
                            error = getString(R.string.edit_message_error_slowpoke);
                        } else if (re.getTag().equals("NOT_LAST_MESSAGE")) {
                            error = getString(R.string.edit_message_error_not_last);
                        } else {
                            error = re.getMessage();
                        }
                        Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                    }
                });
                editRid = 0;
                hideQuote();
            }
            findInputBar().setText("");
        }
    }

    @Override
    public void onAudioSent(int duration, String descriptor) {
        messenger().sendVoice(peer, duration, descriptor);
    }

    @Override
    public void onStickerSent(Sticker sticker) {
        messenger().sendSticker(peer, sticker);
    }

    @Override
    public void onAttachPressed() {
        findInputBar().clearFocus();

        AbsAttachFragment attachFragment = findShareFragment();
        if (attachFragment != null) {
            quoteContainer.post(() -> attachFragment.show());
        }
    }

    @Override
    public void onMessageEdit(long rid, String text) {
        editRid = rid;
        findInputBar().setText(text);
        showQuote(text, false);
    }

    @Override
    public void onMessageQuote(String text) {
        showQuote(text, true);
        currentQuote = text;
    }

    @Override
    public void onQuoteCancelled() {
        editRid = 0;
        hideQuote();
    }

    @Override
    public void onPause() {
        super.onPause();
        messenger().saveDraft(peer, findInputBar().getText());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        quoteContainer = null;
    }


    //
    // Tools
    //
    private InputBarFragment findInputBar() {
        return ((InputBarFragment) getChildFragmentManager().findFragmentById(R.id.sendFragment));
    }

    private QuoteFragment findQuoteFragment() {
        return ((QuoteFragment) getChildFragmentManager().findFragmentById(R.id.quoteFragment));
    }

    private AbsAttachFragment findShareFragment() {
        return ((AbsAttachFragment) getFragmentManager().findFragmentById(R.id.overlay));
    }

    private void hideQuote() {
        hideView(quoteContainer);
        findInputBar().setDisableOnEmptyText(true);
        findInputBar().setAudioEnabled(true);
        findInputBar().setAttachEnabled(true);
    }

    private void showQuote(String text, boolean isQuote) {
        showView(quoteContainer);
        findQuoteFragment().setText(text, isQuote);
        findInputBar().setDisableOnEmptyText(false);
        findInputBar().setAudioEnabled(false);
        findInputBar().setAttachEnabled(false);
    }
}
