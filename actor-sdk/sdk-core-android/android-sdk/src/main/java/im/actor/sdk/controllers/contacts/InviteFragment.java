package im.actor.sdk.controllers.contacts;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.entity.PhoneBookContact;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.fragment.DisplayListFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.adapters.OnItemClickedListener;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class InviteFragment extends DisplayListFragment<PhoneBookContact, InviteContactHolder> {


    private View emptyView;
    private MenuItem sendButton;

    public InviteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateContactsView(R.layout.fragment_base_contacts, inflater, container, savedInstanceState);
    }

    protected View onCreateContactsView(int layoutId, LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflate(inflater, container, layoutId, messenger().buildPhoneBookContactsDisplayList());
        res.findViewById(R.id.collection).setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        emptyView = res.findViewById(R.id.emptyCollection);
        if (emptyView != null) {
            emptyView.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
            emptyView.findViewById(R.id.empty_collection_bg).setBackgroundColor(ActorSDK.sharedActor().style.getMainColor());
            ((TextView) emptyView.findViewById(R.id.empty_collection_text)).setTextColor(ActorSDK.sharedActor().style.getMainColor());
        } else {
            emptyView = res.findViewById(R.id.empty_collection_text);
            if (emptyView != null && emptyView instanceof TextView) {
                ((TextView) emptyView.findViewById(R.id.empty_collection_text)).setTextColor(ActorSDK.sharedActor().style.getMainColor());
            }
        }

        View headerPadding = new View(getActivity());
        headerPadding.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        headerPadding.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(8)));
        addHeaderView(headerPadding);


        if (emptyView != null) {
            if (messenger().getAppState().getIsContactsEmpty().get()) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
        bind(messenger().getAppState().getIsContactsEmpty(), new ValueChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean val, Value<Boolean> Value) {
                if (emptyView != null) {
                    if (val) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                }
            }
        });
        res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        return res;
    }


    @Override
    protected BindedListAdapter<PhoneBookContact, InviteContactHolder> onCreateAdapter(BindedDisplayList<PhoneBookContact> displayList, Activity activity) {
        return new InviteContactAdapter(displayList, activity, new OnItemClickedListener<PhoneBookContact>() {
            @Override
            public void onClicked(PhoneBookContact item) {

                onItemClicked(item);
            }

            @Override
            public boolean onLongClicked(PhoneBookContact item) {
                return false;
            }

        });
    }

    public void onItemClicked(PhoneBookContact contact) {
        long contactId = contact.getContactId();
        boolean selected = isSelected(contactId);
        boolean needDialog = contact.getEmails().size() > 0 && contact.getPhones().size() > 0;

        if (needDialog) {
            String[] items = new String[selected ? 3 : 2];
            items[0] = Long.toString(contact.getPhones().get(0).getNumber());
            items[1] = contact.getEmails().get(0).getEmail();
            if (selected) {
                items[2] = getString(R.string.dialog_cancel);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(items, (dialog, which) -> {
                if (which == 2) {
                    unselect(contactId);
                } else {
                    select(contactId, which);
                }
                getAdapter().notifyDataSetChanged();

                dialog.dismiss();
            }).show();

        } else {
            if (selected) {
                unselect(contactId);
            } else {
                select(contactId, -1);
            }
            getAdapter().notifyDataSetChanged();
        }
    }

    public void select(long id, int type) {
        ((InviteContactAdapter) getAdapter()).select(id, type);
    }

    public void unselect(long id) {
        ((InviteContactAdapter) getAdapter()).unselect(id);
    }

    public boolean isSelected(long id) {
        return ((InviteContactAdapter) getAdapter()).isSelected(id);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.invite, menu);
        sendButton = menu.getItem(0);
        checkSendButton();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.send_invites) {
            sendInvites();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendInvites() {
        ArrayList<Long> phones = new ArrayList<Long>();
        ArrayList<String> emails = new ArrayList<String>();
        Long[] selected = ((InviteContactAdapter) getAdapter()).getSelected();
        HashMap<Long, Integer> selectedTypes = ((InviteContactAdapter) getAdapter()).getSelectedContactsTypes();
        PhoneBookContact contact;
        Integer selectedType;

        //Prepare email/phones lists
        String phoneNumbersStrings = "";
        String emailsString = "";
        String email;
        long number;

        for (long s : selected) {
            contact = messenger().getPhoneBookContact(s);
            selectedType = selectedTypes.get(s);
            selectedType = selectedType != null ? selectedType : InviteContactHolder.TYPE_PHONE;
            if ((selectedType == InviteContactHolder.TYPE_EMAIL && contact.getEmails().size() > 0) || contact.getPhones().size() == 0) {
                email = contact.getEmails().get(0).getEmail();
                emails.add(email);
                emailsString += email + ";";

            } else {
                number = contact.getPhones().get(0).getNumber();
                phones.add(number);
                phoneNumbersStrings += number + ";";
            }

        }

        String inviteMessage = getResources().getString(R.string.invite_message).replace("{inviteUrl}", ActorSDK.sharedActor().getInviteUrl()).replace("{appName}", ActorSDK.sharedActor().getAppName());

        if (phones.size() > 0 && emails.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String[] options = new String[]{getString(R.string.invite_options_sms), getString(R.string.invite_options_email)};
            final String finalPhoneNumbersStrings = phoneNumbersStrings;
            final String finalEmailsString = emailsString;
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        sendSmsInvites(finalPhoneNumbersStrings, inviteMessage);
                    } else {
                        sendEmailInvites(finalEmailsString, inviteMessage);
                    }
                    dialog.dismiss();
                }
            }).show();
        } else if (phones.size() > 0) {
            sendSmsInvites(phoneNumbersStrings, inviteMessage);

        } else {
            sendEmailInvites(emailsString, inviteMessage);
        }

//        SmsManager smsManager = SmsManager.getDefault();
//        smsManager.sendTextMessage(phoneNumbers, null, inviteMessage, null, null);
    }

    private void sendEmailInvites(String emailsString, String inviteMessage) {
        Uri emailsToUri = Uri.parse("mailto:" + emailsString);
        Intent i = new Intent(Intent.ACTION_SENDTO, emailsToUri);
        i.putExtra(Intent.EXTRA_TEXT, inviteMessage);
        startActivity(Intent.createChooser(i, getString(R.string.contacts_invite_via_link)));
    }

    private void sendSmsInvites(String phoneNumbersStrings, String inviteMessage) {
        Uri smsToUri = Uri.parse("smsto:" + phoneNumbersStrings);
        Intent i = new Intent(Intent.ACTION_SENDTO, smsToUri);
        i.putExtra("sms_body", inviteMessage);
        startActivity(i);
    }

    public void checkSendButton() {
        if (sendButton != null) {
            sendButton.setVisible(getDisplayList().getSize() != 0);
        }
    }

    @Override
    public void onCollectionChanged() {
        super.onCollectionChanged();
        checkSendButton();
        InviteContactAdapter adapter = (InviteContactAdapter) getAdapter();
        if (adapter != null) {
            adapter.updateIds();
        }
    }

}
