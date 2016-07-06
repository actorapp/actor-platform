package im.actor.sdk.controllers.placeholder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.contacts.AddContactActivity;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.util.Fonts;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class GlobalPlaceholderFragment extends BaseFragment {

    private View syncInProgressView;
    private View emptyContactsView;

    public GlobalPlaceholderFragment() {
        setUnbindOnPause(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_placeholder, container, false);

        //
        // Views
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
        // Actions
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

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();

        bind(messenger().getAppState().getIsAppLoaded(), messenger().getAppState().getIsAppEmpty(), (isAppLoaded, Value, isAppEmpty, Value2) -> {
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
        });
    }

}
