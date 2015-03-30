package im.actor.messenger.app.fragment.contacts;

import android.app.AlertDialog;
import android.content.DialogInterface;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.Contact;

import static im.actor.messenger.app.Core.messenger;

public class ContactsFragment extends BaseContactFragment {

    public ContactsFragment() {
        super(false, false, false);
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
