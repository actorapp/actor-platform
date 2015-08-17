package im.actor.messenger.app.fragment.contacts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.core.entity.Contact;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;

import static im.actor.messenger.app.core.Core.messenger;

public class ContactsFragment extends BaseContactFragment {

    public ContactsFragment() {
        super(false, false, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = onCreateContactsView(R.layout.fragment_contacts, inflater, container, savedInstanceState);
        res.findViewById(R.id.inviteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inviteMessage = getResources().getString(R.string.invite_message);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, inviteMessage);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
        return res;
    }

    @Override
    public void onItemClicked(Contact contact) {
        getActivity().startActivity(Intents.openPrivateDialog(contact.getUid(), true, getActivity()));
    }

    @Override
    public boolean onItemLongClicked(final Contact contact) {
        new AlertDialog.Builder(getActivity())
                .setItems(new CharSequence[]{
                        getString(R.string.contacts_menu_remove).replace("{0}", contact.getName()),
                        getString(R.string.contacts_menu_edit),
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(getString(R.string.alert_remove_contact_text).replace("{0}", contact.getName()))
                                    .setPositiveButton(R.string.alert_remove_contact_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            execute(messenger().removeContact(contact.getUid()), R.string.contacts_menu_remove_progress, new CommandCallback<Boolean>() {
                                                @Override
                                                public void onResult(Boolean res) {

                                                }

                                                @Override
                                                public void onError(Exception e) {

                                                }
                                            });
                                        }
                                    })
                                    .setNegativeButton(R.string.dialog_cancel, null)
                                    .show()
                                    .setCanceledOnTouchOutside(true);
                        } else if (which == 1) {
                            startActivity(Intents.editUserName(contact.getUid(), getActivity()));
                        }
                    }
                })
                .show()
                .setCanceledOnTouchOutside(true);
        return true;
    }
}
