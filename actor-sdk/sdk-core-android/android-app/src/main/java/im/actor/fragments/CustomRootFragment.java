package im.actor.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.sdk.controllers.dialogs.DialogsDefaultFragment;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.placeholder.GlobalPlaceholderFragment;
import im.actor.sdk.controllers.search.GlobalSearchDefaultFragment;
import im.actor.sdk.controllers.search.GlobalSearchStateDelegate;

public class CustomRootFragment extends BaseFragment implements GlobalSearchStateDelegate {

    // private BottomBar bottomBar;

    public CustomRootFragment() {
        setRootFragment(true);
        setUnbindOnPause(true);
        setTitle(im.actor.sdk.R.string.app_name);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View res = inflater.inflate(im.actor.sdk.R.layout.activity_root_content, container, false);

//        bottomBar = BottomBar.attach(getActivity(), savedInstanceState);
//        bottomBar.setItems(
//                new BottomBarTab(R.drawable.ic_home_black_24dp, "Home"),
//                new BottomBarTab(R.drawable.ic_message_black_24dp, "Recent"),
//                new BottomBarTab(R.drawable.ic_contacts_black_24dp, "Contacts"),
//                new BottomBarTab(R.drawable.ic_settings_black_24dp, "Settings"));

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(im.actor.sdk.R.id.content, buildDialogsFragment())
                    .add(im.actor.sdk.R.id.search, new GlobalSearchDefaultFragment())
                    .add(im.actor.sdk.R.id.placeholder, new GlobalPlaceholderFragment())
                    .commit();
        }

//        bottomBar.setOnTabClickListener(new OnTabClickListener() {
//            @Override
//            public void onTabSelected(int position) {
//                if (position == 0) {
//                    getChildFragmentManager().beginTransaction()
//                            .setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
//                            .replace(im.actor.sdk.R.id.content, new EmptyFragment())
//                            .commit();
//                } else if (position == 1) {
//                    getChildFragmentManager().beginTransaction()
//                            .setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
//                            .replace(im.actor.sdk.R.id.content, buildDialogsFragment())
//                            .commit();
//                } else if (position == 2) {
//                    getChildFragmentManager().beginTransaction()
//                            .setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
//                            .replace(im.actor.sdk.R.id.content, new ContactsFragment())
//                            .commit();
//                } else if (position == 3) {
//                    getChildFragmentManager().beginTransaction()
//                            .setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
//                            .replace(im.actor.sdk.R.id.content, new ActorSettingsFragment())
//                            .commit();
//                }
//            }
//
//            @Override
//            public void onTabReSelected(int position) {
//
//            }
//        });

        return res;
    }

    private DialogsDefaultFragment buildDialogsFragment() {
        DialogsDefaultFragment res = new DialogsDefaultFragment();
        res.setRootFragment(true);
        res.setHasOptionsMenu(false);
        res.setTitle(im.actor.sdk.R.string.app_name);
        return res;
    }

    @Override
    public void onResume() {
        super.onResume();

//        bind(messenger().getAppState().getIsAppLoaded(), messenger().getAppState().getIsAppEmpty(), (isAppLoaded, Value, isAppEmpty, Value2) -> {
//            if (isAppEmpty && !isAppLoaded) {
//                bottomBar.hide();
//            } else {
//                bottomBar.show();
//            }
//        });
    }

    @Override
    public void onGlobalSearchStarted() {
//        if (bottomBar != null) {
//            bottomBar.hide();
//        }
    }

    @Override
    public void onGlobalSearchEnded() {
//        if (bottomBar != null) {
//            bottomBar.show();
//        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // bottomBar.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // bottomBar = null;
    }
}
