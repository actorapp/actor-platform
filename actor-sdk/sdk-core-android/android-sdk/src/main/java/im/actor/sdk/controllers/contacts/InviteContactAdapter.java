package im.actor.sdk.controllers.contacts;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.entity.Contact;
import im.actor.core.entity.PhoneBookContact;
import im.actor.core.entity.PhoneBookIds;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.sdk.controllers.contacts.view.ContactHolder;
import im.actor.sdk.view.adapters.OnItemClickedListener;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class InviteContactAdapter extends BindedListAdapter<PhoneBookContact, InviteContactHolder> {

    private final HashSet<Long> selectedUsers = new HashSet<Long>();
    private final HashMap<Long, Integer> selectedContactType = new HashMap<Long, Integer>();

    private final OnItemClickedListener<PhoneBookContact> onItemClickedListener;


    private final Context context;

    private String query = "";

    public InviteContactAdapter(BindedDisplayList<PhoneBookContact> displayList, Context context,
                                OnItemClickedListener<PhoneBookContact> onItemClickedListener) {
        super(displayList);
        this.context = context;
        this.onItemClickedListener = onItemClickedListener;

        try {
            PhoneBookIds ids = Bser.parse(new PhoneBookIds(), messenger().getPreferences().getBytes("phone_book_ids"));
            for (long id : ids.getIds()) {
                selectedUsers.add(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setQuery(String query) {
        this.query = query;
        notifyDataSetChanged();
    }

    public void select(long id, int type) {
        selectedUsers.add(id);
        selectedContactType.put(id, type);
    }

    public void unselect(long uid) {
        selectedUsers.remove(uid);
    }

    public Long[] getSelected() {
        return selectedUsers.toArray(new Long[selectedUsers.size()]);
    }

    public HashMap<Long, Integer> getSelectedContactsTypes() {
        return selectedContactType;
    }

    public boolean isSelected(long id) {
        return selectedUsers.contains(id);
    }

    public int getSelectedCount() {
        return selectedUsers.size();
    }

    @Override
    public void onBindViewHolder(InviteContactHolder contactHolder, int index, PhoneBookContact item) {
        String fastName = null;
        if (index == 0) {
            fastName = messenger().getFormatter().formatFastName(item.getName());
        } else {
            String prevName = messenger().getFormatter().formatFastName(getItem(index - 1).getName());
            String currentFastName = messenger().getFormatter().formatFastName(item.getName());
            if (!prevName.equals(currentFastName)) {
                fastName = currentFastName;
            }
        }
        Integer type = selectedContactType.get(item.getContactId());
        contactHolder.bind(item, fastName, query, selectedUsers.contains(item.getContactId()), type == null ? -1 : type, index == getItemCount() - 1);
    }

    @Override
    public InviteContactHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new InviteContactHolder(new FrameLayout(context), context, onItemClickedListener);
    }
}
