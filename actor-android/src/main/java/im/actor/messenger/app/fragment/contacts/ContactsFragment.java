package im.actor.messenger.app.fragment.contacts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.droidkit.engine.list.view.ListState;
import com.droidkit.mvvm.ui.Listener;
import com.droidkit.mvvm.ui.Processor;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.messenger.core.actors.base.UiAskCallback;
import im.actor.messenger.core.actors.contacts.ContactsActor;
import im.actor.messenger.model.ContactsSyncState;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.scheme.Contact;
import im.actor.messenger.util.Screen;

public class ContactsFragment extends BaseCompatFragment implements Listener<ListState> {

    private ContactsAdapter adapter;
    private ListView listView;
    private View noContacts;
    private ImageView emptyContactsImage;

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_contacts, container, false);

        emptyContactsImage = (ImageView) res.findViewById(R.id.emptyContactsImage);

        View progress = res.findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

        listView = (ListView) res.findViewById(R.id.contactsList);

        TextView header = new TextView(getActivity());
        header.setBackgroundColor(getResources().getColor(R.color.bg_grey));
        header.setText(R.string.contacts_title);
        header.setTypeface(Fonts.bold());
        header.setPadding(Screen.dp(16), 0, 0, 0);
        header.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        header.setTextSize(16);
        header.setTextColor(getResources().getColor(R.color.text_subheader));

        LinearLayout headerCont = new LinearLayout(getActivity());
        headerCont.setBackgroundColor(getResources().getColor(R.color.bg_light));
        headerCont.setOrientation(LinearLayout.VERTICAL);
        headerCont.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(48)));

        listView.addHeaderView(headerCont, null, false);

        FrameLayout footer = new FrameLayout(getActivity());
        footer.setBackgroundColor(getResources().getColor(R.color.bg_grey));
        footer.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(112)));
        ImageView shadow = new ImageView(getActivity());
        shadow.setImageResource(R.drawable.card_shadow_bottom);
        shadow.setScaleType(ImageView.ScaleType.FIT_XY);
        shadow.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(4)));
        footer.addView(shadow);

        listView.addFooterView(footer, null, false);

        getBinder().bind(progress, ContactsSyncState.getSyncState(), new Processor<View, Boolean>() {
            @Override
            public void process(View obj, Boolean val) {
                if (val) {
                    showView(obj);
                } else {
                    hideView(obj);
                }
            }
        });

        adapter = new ContactsAdapter(ListEngines.getContactsList(), getActivity(), false,
                new OnItemClickedListener<Contact>() {
                    @Override
                    public void onClicked(Contact item) {
                        onClick(item);
                    }
                }, new OnItemClickedListener<Contact>() {
            @Override
            public void onClicked(Contact item) {
                onLongClick(item);
            }
        });
        listView.setAdapter(adapter);
        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                adapter.onMovedToScrapHeap(view);
            }
        });

        noContacts = res.findViewById(R.id.noContacts);

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

        getBinder().bind(ContactsFragment.this, ListEngines.getContactsList().getListState(), ContactsFragment.this);
        return res;
    }

    private void onLongClick(final Contact contact) {
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
                                            ask(ContactsActor.contactsList().removeContact(contact.getUid()), getString(R.string.contacts_menu_remove_progress), new UiAskCallback<Boolean>() {
                                                @Override
                                                public void onPreStart() {

                                                }

                                                @Override
                                                public void onCompleted(Boolean res) {

                                                }

                                                @Override
                                                public void onError(Throwable t) {

                                                }
                                            });
                                        }
                                    })
                                    .setNegativeButton(R.string.dialog_cancel, null)
                                    .show()
                                    .setCanceledOnTouchOutside(true);
                        } else {
                            startActivity(Intents.editUserName(contact.getUid(), getActivity()));
                        }
                    }
                })
                .show()
                .setCanceledOnTouchOutside(true);
    }

    private void onClick(Contact contact) {
        getActivity().startActivity(Intents.openPrivateDialog(contact.getUid(), true, getActivity()));
    }

    @Override
    public void onUpdated(ListState listState) {
        switch (listState.getState()) {
            case LOADED:
                goneView(noContacts);
                showView(listView);
                break;
            case LOADED_EMPTY:
                showView(noContacts);
                goneView(listView);
                break;
            case LOADING_EMPTY:
                goneView(noContacts);
                goneView(listView);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        emptyContactsImage.setImageResource(R.drawable.contacts_empty_large);
    }

    @Override
    public void onPause() {
        super.onPause();
        emptyContactsImage.setImageBitmap(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        listView = null;
        noContacts = null;
        emptyContactsImage = null;
    }

}
