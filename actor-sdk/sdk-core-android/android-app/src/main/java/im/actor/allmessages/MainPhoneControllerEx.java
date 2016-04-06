package im.actor.allmessages;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import im.actor.R;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.activity.controllers.MainPhoneController;

public class MainPhoneControllerEx extends MainPhoneController{
    public MainPhoneControllerEx(ActorMainActivity mainActivity) {
        super(mainActivity);
    }

    @NonNull
    @Override
    public HomePagerAdapter getHomePagerAdapter() {
        return new HomePagerAdapterEx(getFragmentManager());
    }

    private class HomePagerAdapterEx extends HomePagerAdapter{

        public HomePagerAdapterEx(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                default:
                case 0:
                    return getDialogsFragment();
                case 1:
                    return getContactsFragment();
                case 2:
                    return new AllMessagesFragment();

            }
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                default:
                case 0:
                    return getActivity().getString(im.actor.sdk.R.string.main_bar_chats);
                case 1:
                    return getActivity().getString(im.actor.sdk.R.string.main_bar_contacts);
            }
        }

        @Override
        public int getPageIconResId(int position, Context context) {
            return (position == 2? R.drawable.ic_favorite_white_36dp    :-1);
        }
    }
}
