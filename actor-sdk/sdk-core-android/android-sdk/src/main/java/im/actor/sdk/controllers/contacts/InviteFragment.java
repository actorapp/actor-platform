package im.actor.sdk.controllers.contacts;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

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

}
