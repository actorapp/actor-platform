package im.actor.messenger.app.fragment.contacts;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.HashSet;

import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.model.android.BindedListAdapter;
import im.actor.model.entity.Contact;
import im.actor.model.mvvm.BindedDisplayList;

public class ContactsAdapter extends BindedListAdapter<Contact, ContactHolder> {

    private boolean selectable;

    private HashSet<Integer> selectedUsers = new HashSet<Integer>();

    private String query = "";

    private OnItemClickedListener<Contact> onItemClickedListener;
    private OnItemClickedListener<Contact> onItemLongClickedListener;

    private Context context;

    public ContactsAdapter(BindedDisplayList<Contact> displayList, Context context, boolean selectable) {
        super(displayList);
        this.context = context;
        this.selectable = selectable;
    }

    public void setOnItemClickedListener(OnItemClickedListener<Contact> onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public void setOnItemLongClickedListener(OnItemClickedListener<Contact> onItemLongClickedListener) {
        this.onItemLongClickedListener = onItemLongClickedListener;
    }

    public void setQuery(String query) {
        this.query = query;
        notifyDataSetChanged();
    }

    public void select(int uid) {
        selectedUsers.add(uid);
    }

    public void unselect(int uid) {
        selectedUsers.remove(uid);
    }

    @Override
    public void onBindViewHolder(ContactHolder dialogHolder, int index, Contact item) {
        dialogHolder.bind(item);
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup viewGroup, int index, Contact item) {
        return new ContactHolder(new FrameLayout(context), selectable, context);
    }
}
