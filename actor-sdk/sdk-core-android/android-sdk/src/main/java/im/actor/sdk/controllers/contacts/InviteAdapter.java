package im.actor.sdk.controllers.contacts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import im.actor.core.entity.PhoneBookContact;
import im.actor.core.entity.PhoneBookIds;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.bser.Bser;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.OnItemClickedListener;
import im.actor.sdk.view.adapters.RecyclerListView;
import im.actor.sdk.view.adapters.ViewHolder;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class InviteAdapter extends HolderAdapter<PhoneBookContact> {

    List<PhoneBookContact> phoneBook;
    Context context;

    private final HashSet<PhoneBookContact> selectedUsers = new HashSet<PhoneBookContact>();
    private final HashMap<PhoneBookContact, Integer> selectedContactType = new HashMap<PhoneBookContact, Integer>();

    private final OnItemClickedListener<PhoneBookContact> onItemClickedListener;


    public InviteAdapter(Context context, List<PhoneBookContact> phoneBook, OnItemClickedListener<PhoneBookContact> onItemClickedListener) {
        super(context);
        this.phoneBook = phoneBook;
        selectedUsers.addAll(phoneBook);
        this.context = context;
        this.onItemClickedListener = onItemClickedListener;
    }


    @Override
    protected void onBindViewHolder(ViewHolder<PhoneBookContact> holder, PhoneBookContact obj, int index, Context context) {
        PhoneBookContact item = phoneBook.get(index);
        String fastName = null;
        if (index == 0) {
            fastName = messenger().getFormatter().formatFastName(item.getName());
        } else {
            String prevName = messenger().getFormatter().formatFastName(phoneBook.get(index - 1).getName());
            String currentFastName = messenger().getFormatter().formatFastName(item.getName());
            if (!prevName.equals(currentFastName)) {
                fastName = currentFastName;
            }
        }
        Integer type = selectedContactType.get(item);
        ((InviteContactHolder) holder).bind(item, fastName, "", selectedUsers.contains(item), type == null ? -1 : type, index == getCount() - 1);
    }



    public void select(PhoneBookContact contact, int type) {
        selectedUsers.add(contact);
        selectedContactType.put(contact, type);
    }

    public void unselect(PhoneBookContact uid) {
        selectedUsers.remove(uid);
    }

    public PhoneBookContact[] getSelected() {
        return selectedUsers.toArray(new PhoneBookContact[selectedUsers.size()]);
    }

    public HashMap<PhoneBookContact, Integer> getSelectedContactsTypes() {
        return selectedContactType;
    }

    public boolean isSelected(PhoneBookContact id) {
        return selectedUsers.contains(id);
    }

    @Override
    public int getCount() {
        return phoneBook.size();
    }

    @Override
    public PhoneBookContact getItem(int position) {
        return phoneBook.get(position);
    }

    @Override
    public long getItemId(int position) {
        return phoneBook.get(position).getContactId();
    }

    @Override
    protected ViewHolder<PhoneBookContact> createHolder(PhoneBookContact obj) {
        return new InviteContactHolder(new FrameLayout(context), context, onItemClickedListener);
    }
}
