package im.actor.sdk.controllers.conversation.mentions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import im.actor.core.entity.BotCommand;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.MaterialInterpolator;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.RecyclerListView;

import static im.actor.sdk.util.ActorSDKMessenger.users;

public class AutocompleteFragment extends BaseFragment {

    private Peer peer;
    private boolean isBot;
    private boolean isGroup;

    private HolderAdapter autocompleteAdapter;
    private RecyclerListView autocompleteList;

    public static AutocompleteFragment create(Peer peer) {
        AutocompleteFragment res = new AutocompleteFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("peer", peer.getUnuqueId());
        res.setArguments(bundle);
        return res;
    }

    public AutocompleteFragment() {

    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        peer = Peer.fromUniqueId(getArguments().getLong("peer"));
        if (peer.getPeerType() == PeerType.PRIVATE) {
            isBot = users().get(peer.getPeerId()).isBot();
            autocompleteAdapter = new CommandsAdapter(peer.getPeerId(), getContext());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            isGroup = true;
            autocompleteAdapter = new MentionsAdapter(peer.getPeerId(), getContext());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        autocompleteList = new RecyclerListView(getContext());
        autocompleteList.setDivider(null);
        autocompleteList.setDividerHeight(0);
        autocompleteList.setBackgroundColor(style.getMainBackgroundColor());
        if (autocompleteAdapter != null) {
            autocompleteList.setAdapter(autocompleteAdapter);
        }
        autocompleteList.setOnItemClickListener((adapterView, view, i, l) -> {
            Object item = autocompleteAdapter.getItem(i);
            if (item instanceof MentionFilterResult) {
                String mention = ((MentionFilterResult) item).getMentionString();
                Fragment parent = getParentFragment();
                if (parent instanceof AutocompleteCallback) {
                    ((AutocompleteCallback) parent).onMentionPicked(mention);
                }
            } else if (item instanceof BotCommand) {
                String command = ((BotCommand) item).getSlashCommand();
                Fragment parent = getParentFragment();
                if (parent instanceof AutocompleteCallback) {
                    ((AutocompleteCallback) parent).onCommandPicked(command);
                }
            }
        });

        // Initial zero height
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.gravity = Gravity.BOTTOM;
        autocompleteList.setLayoutParams(params);

        return autocompleteList;
    }

    public void onCurrentWordChanged(String text) {
        if (isBot) {
            if (text.startsWith("/")) {
                String query = text.substring(1);
                int oldCount = autocompleteList.getCount();
                ((CommandsAdapter) autocompleteAdapter).setQuery(query);
                expandMentions(autocompleteList, oldCount, autocompleteList.getCount());
            } else {
                int oldCount = autocompleteList.getCount();
                ((CommandsAdapter) autocompleteAdapter).clearQuery();
                expandMentions(autocompleteList, oldCount, autocompleteList.getCount());
            }
        } else if (isGroup) {
            if (text.startsWith("@")) {
                String query = text.substring(1);
                int oldCount = autocompleteList.getCount();
                ((MentionsAdapter) autocompleteAdapter).setQuery(query);
                expandMentions(autocompleteList, oldCount, autocompleteList.getCount());
            } else {
                int oldCount = autocompleteList.getCount();
                ((MentionsAdapter) autocompleteAdapter).clearQuery();
                expandMentions(autocompleteList, oldCount, autocompleteList.getCount());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (autocompleteAdapter != null) {
            autocompleteAdapter.dispose();
        }
        autocompleteList = null;
    }


    //
    // Expand Animations
    //

    private void expandMentions(final View v, final int oldRowsCount, final int newRowsCount) {
        if (newRowsCount == oldRowsCount) {
            return;
        }

        v.measure(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        int newRowsHeight = Screen.dp(48) * newRowsCount + newRowsCount;

        final int targetHeight = (newRowsHeight) > Screen.dp(96 + 2) ? Screen.dp(122) : newRowsHeight;
        final int initialHeight = v.getLayoutParams().height;

        v.getLayoutParams().height = initialHeight;
        v.setVisibility(View.VISIBLE);
        Animation a = new ExpandAnimation(v, targetHeight, initialHeight);

        a.setDuration((newRowsCount > oldRowsCount ? targetHeight : initialHeight / Screen.dp(1)));
        a.setInterpolator(MaterialInterpolator.getInstance());
        v.startAnimation(a);
    }

    private static class ExpandAnimation extends Animation {

        private final View v;
        private final int targetHeight;
        private final int initialHeight;
        private int currentHeight;

        public ExpandAnimation(View v, int targetHeight, int initialHeight) {
            this.v = v;
            this.targetHeight = targetHeight;
            this.initialHeight = initialHeight;
            this.currentHeight = initialHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (targetHeight > initialHeight) {
                currentHeight =
                        (int) ((targetHeight * interpolatedTime) - initialHeight * interpolatedTime + initialHeight);
            } else {
                currentHeight =
                        (int) (initialHeight - (initialHeight * interpolatedTime) - targetHeight * (1f - interpolatedTime) + targetHeight);
            }

            v.getLayoutParams().height = currentHeight;
            v.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
