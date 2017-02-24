package im.actor.sdk.controllers.root;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.compose.ComposeFabFragment;
import im.actor.sdk.controllers.contacts.ContactsActivity;
import im.actor.sdk.controllers.dialogs.DialogsDefaultFragment;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.fragment.help.HelpActivity;
import im.actor.sdk.controllers.placeholder.GlobalPlaceholderFragment;
import im.actor.sdk.controllers.search.GlobalSearchDefaultFragment;

public class RootFragment extends BaseFragment {

    public RootFragment() {
        setRootFragment(true);
        setUnbindOnPause(true);
        setTitle(ActorSDK.sharedActor().getAppName());
    }

    private boolean isInited = false;

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        if (saveInstance != null) {
            isInited = saveInstance.getBoolean("is_inited");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FrameLayout res = new FrameLayout(getContext());

        FrameLayout content = new FrameLayout(getContext());
        content.setId(R.id.content);
        res.addView(content, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout fab = new FrameLayout(getContext());
        fab.setId(R.id.fab);
        res.addView(fab, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout search = new FrameLayout(getContext());
        search.setId(R.id.search);
        res.addView(search, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout placeholder = new FrameLayout(getContext());
        placeholder.setId(R.id.placeholder);
        res.addView(placeholder, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        return res;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isInited) {
            isInited = true;
            DialogsDefaultFragment dialogsDefaultFragment = ActorSDK.sharedActor().getDelegate().fragmentForDialogs();
            getChildFragmentManager().beginTransaction()
                    .add(R.id.content, dialogsDefaultFragment != null ? dialogsDefaultFragment : new DialogsDefaultFragment())
                    .add(R.id.fab, new ComposeFabFragment())
                    .add(R.id.search, new GlobalSearchDefaultFragment())
                    .add(R.id.placeholder, new GlobalPlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_inited", isInited);
    }
}
