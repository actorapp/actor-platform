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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View res = inflater.inflate(R.layout.activity_root_content, container, false);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(R.id.content, new DialogsDefaultFragment())
                    .add(R.id.fab, new ComposeFabFragment())
                    .add(R.id.search, new GlobalSearchDefaultFragment())
                    .add(R.id.placeholder, new GlobalPlaceholderFragment())
                    .commit();
        }

        return res;
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
}
