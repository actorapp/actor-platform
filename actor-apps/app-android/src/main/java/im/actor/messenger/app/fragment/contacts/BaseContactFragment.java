package im.actor.messenger.app.fragment.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.core.entity.Contact;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.messenger.R;
import im.actor.messenger.app.activity.AddContactActivity;
import im.actor.messenger.app.fragment.DisplayListFragment;
import im.actor.messenger.app.fragment.help.HelpActivity;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.messenger.app.view.TintImageView;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by ex3ndr on 23.09.14.
 */
public abstract class BaseContactFragment extends DisplayListFragment<Contact, ContactHolder> {

    private View emptyView;
    private final boolean useCompactVersion;
    private final boolean userSearch;
    private final boolean useSelection;

    public BaseContactFragment(boolean useCompactVersion, boolean userSearch, boolean useSelection) {
        this.useCompactVersion = useCompactVersion;
        this.userSearch = userSearch;
        this.useSelection = useSelection;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateContactsView(R.layout.fragment_base_contacts, inflater, container, savedInstanceState);
    }

    protected View onCreateContactsView(int layoutId, LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflate(inflater, container, layoutId, messenger().buildContactsDisplayList());

        emptyView = res.findViewById(R.id.emptyCollection);

        View headerPadding = new View(getActivity());
        headerPadding.setBackgroundColor(getResources().getColor(R.color.bg_main));
        headerPadding.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(0)));
        addHeaderView(headerPadding);

        addFootersAndHeaders();

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

        return res;
    }

    protected void addFootersAndHeaders() {
        if (useCompactVersion) {
            View footer = new View(getActivity());
            footer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(4)));
            addFooterView(footer);
        } else {
            addFooterOrHeaderAction(R.color.contacts_action_share, R.drawable.ic_share_white_24dp, R.string.contacts_share, false, new Runnable() {
                @Override
                public void run() {
                    String inviteMessage = getResources().getString(R.string.invite_message);
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteMessage);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            }, false);

            addFooterOrHeaderAction(R.color.contacts_action_add, R.drawable.ic_person_add_white_24dp, R.string.contacts_add, true, new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getActivity(), AddContactActivity.class));
                }
            }, false);

            FrameLayout footer = new FrameLayout(getActivity());
            footer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(112)));
            ImageView shadow = new ImageView(getActivity());
            shadow.setImageResource(R.drawable.card_shadow_bottom);
            shadow.setScaleType(ImageView.ScaleType.FIT_XY);
            shadow.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(4)));
            footer.addView(shadow);

            addFooterView(footer);
        }
    }

    protected void addFooterOrHeaderAction(int color, int icon, int text, boolean isLast, final Runnable action, boolean isHeader) {
        FrameLayout container = new FrameLayout(getActivity());
        container.setBackgroundColor(getResources().getColor(R.color.bg_main));
        {
            container.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        FrameLayout invitePanel = new FrameLayout(getActivity());
        invitePanel.setBackgroundResource(R.drawable.selector_fill);
        invitePanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.run();
            }
        });
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(64));
            params.leftMargin = Screen.dp(40);
            invitePanel.setLayoutParams(params);
            container.addView(invitePanel);
        }

        TintImageView inviteIcon = new TintImageView(getActivity());
        inviteIcon.setTint(getResources().getColor(color));
        inviteIcon.setResource(icon);
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(52), Screen.dp(52));
            layoutParams.leftMargin = Screen.dp(6);
            layoutParams.topMargin = Screen.dp(6);
            layoutParams.bottomMargin = Screen.dp(6);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            invitePanel.addView(inviteIcon, layoutParams);
        }

        TextView inviteText = new TextView(getActivity());
        inviteText.setText(text);
        inviteText.setTextColor(getResources().getColor(color));
        inviteText.setPadding(Screen.dp(72), 0, Screen.dp(8), 0);
        inviteText.setTextSize(16);
        inviteText.setSingleLine(true);
        inviteText.setEllipsize(TextUtils.TruncateAt.END);
        inviteText.setTypeface(Fonts.medium());
        {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.topMargin = Screen.dp(16);
            layoutParams.bottomMargin = Screen.dp(16);
            invitePanel.addView(inviteText, layoutParams);
        }

        if (!isLast) {
            View div = new View(getActivity());
            div.setBackgroundColor(getResources().getColor(R.color.contacts_divider));
            {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        getResources().getDimensionPixelSize(R.dimen.div_size));
                layoutParams.gravity = Gravity.BOTTOM;
                layoutParams.leftMargin = Screen.dp(72);
                invitePanel.addView(div, layoutParams);
            }
        }
        if(isHeader){
            addHeaderView(container);
        }else{
            addFooterView(container);
        }
    }

    @Override
    protected BindedListAdapter<Contact, ContactHolder> onCreateAdapter(BindedDisplayList<Contact> displayList, Activity activity) {
        return new ContactsAdapter(displayList, activity, useSelection, new OnItemClickedListener<Contact>() {
            @Override
            public void onClicked(Contact item) {
                onItemClicked(item);
            }

            @Override
            public boolean onLongClicked(Contact item) {
                return onItemLongClicked(item);
            }
        });
    }

    public void onItemClicked(Contact contact) {

    }

    public boolean onItemLongClicked(Contact contact) {
        return false;
    }

    public void filter(String query) {
        query = query.trim();
        if (query.length() == 0) {
            getDisplayList().initTop(false);
        } else {
            getDisplayList().initSearch(query, false);
        }
        ((ContactsAdapter) getAdapter()).setQuery(query.toLowerCase());
    }


    public void select(int uid) {
        ((ContactsAdapter) getAdapter()).select(uid);
    }

    public void unselect(int uid) {
        ((ContactsAdapter) getAdapter()).unselect(uid);
    }

    public Integer[] getSelected() {
        return ((ContactsAdapter) getAdapter()).getSelected();
    }

    public int getSelectedCount() {
        return ((ContactsAdapter) getAdapter()).getSelectedCount();
    }

    public boolean isSelected(int uid) {
        return ((ContactsAdapter) getAdapter()).isSelected(uid);
    }

    // Search menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (userSearch) {
            inflater.inflate(R.menu.compose, menu);

            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filter(newText);
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (userSearch) {
            if (item.getItemId() == R.id.help) {
                startActivity(new Intent(getActivity(), HelpActivity.class));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
