package im.actor.messenger.app.fragment.compose;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.*;
import android.text.style.ReplacementSpan;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.droidkit.engine.list.view.EngineUiList;
import com.droidkit.engine.uilist.UiListListener;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.fragment.contacts.ContactsAdapter;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.messenger.core.actors.base.UiAskCallback;
import im.actor.messenger.core.actors.groups.GroupAvatarActor;
import im.actor.messenger.core.actors.groups.GroupsActor;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.scheme.Contact;
import im.actor.messenger.util.BoxUtil;
import im.actor.messenger.util.Screen;

import java.util.ArrayList;

import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 04.10.14.
 */
public class GroupUsersFragment extends BaseCompatFragment implements UiListListener {

    public static GroupUsersFragment create(String title, String avatarPath) {
        GroupUsersFragment res = new GroupUsersFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("avatarPath", avatarPath);
        res.setArguments(args);
        return res;
    }

    private ContactsAdapter adapter;
    private ListView listView;
    private EditText searchField;
    private EngineUiList<Contact> engineUiList;
    private ArrayList<Integer> selectedUsers = new ArrayList<Integer>();
    private TextWatcher textWatcher;

    private View progressView;
    private boolean isInProgress;

    private String title;
    private String avatarPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        title = getArguments().getString("title");
        avatarPath = getArguments().getString("avatarPath");

        View res = inflater.inflate(R.layout.fragment_create_group_participants, container, false);
        progressView = res.findViewById(R.id.progress);
        progressView.setVisibility(View.GONE);
        searchField = (EditText) res.findViewById(R.id.searchField);
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                checkForDeletions(s);

                String filter = s.toString().trim();
                while (filter.length() > 0 && filter.charAt(0) == '!') {
                    filter = filter.substring(1);
                }
                engineUiList.filter(filter);
            }
        };
        listView = (ListView) res.findViewById(R.id.contactsList);
        View header = new View(getActivity());
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(4)));
        listView.addHeaderView(header, null, false);
        engineUiList = new EngineUiList<Contact>(ListEngines.getContactsEngine());
        adapter = new ContactsAdapter(engineUiList, getActivity(), true, new OnItemClickedListener<Contact>() {
            @Override
            public void onClicked(Contact contact) {
                if (selectedUsers.contains(contact.getUid())) {
                    selectedUsers.remove((Integer) contact.getUid());
                    adapter.unselect(contact.getUid());
                } else {
                    selectedUsers.add(contact.getUid());
                    adapter.select(contact.getUid());
                }
                getActivity().invalidateOptionsMenu();
                updateEditText();
            }
        }, null);
        listView.setAdapter(adapter);
        engineUiList.getUiList().addListener(this);
        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        searchField.addTextChangedListener(textWatcher);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.create_group, menu);
        if (isInProgress) {
            menu.findItem(R.id.done).setVisible(false);
        } else {
            menu.findItem(R.id.done).setVisible(true);
            menu.findItem(R.id.done).setEnabled(selectedUsers.size() > 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            ask(GroupsActor.groupUpdates().createGroup(title, BoxUtil.unbox(selectedUsers.toArray(new Integer[0]))),
                    new UiAskCallback<Integer>() {
                        @Override
                        public void onPreStart() {
                            isInProgress = true;
                            getActivity().invalidateOptionsMenu();
                            showView(progressView);
                        }

                        @Override
                        public void onCompleted(Integer res) {
                            if (avatarPath != null) {
                                GroupAvatarActor.get().changeAvatar(res, avatarPath);
                            }
                            getActivity().startActivity(Intents.openGroupDialog(res, true, getActivity()));
                            getActivity().finish();
                        }

                        @Override
                        public void onError(Throwable t) {
                            isInProgress = false;
                            getActivity().invalidateOptionsMenu();
                            goneView(progressView);
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListUpdated() {
        if (engineUiList.getUiList().getSize() == 1) {
            Contact contact = engineUiList.getUiList().getItem(0);
            if (!selectedUsers.contains(contact.getUid())) {
                selectedUsers.add(contact.getUid());
                adapter.select(contact.getUid());
                getActivity().invalidateOptionsMenu();
                updateEditText();
            }
        }
    }

    private void updateEditText() {
        String src = "";
        for (int i = 0; i < selectedUsers.size(); i++) {
            src += "!";
        }
        Spannable spannable = new SpannableString(src);
        for (int i = 0; i < selectedUsers.size(); i++) {
            spannable.setSpan(new UserSpan(users().get(selectedUsers.get(i)), Screen.dp(200)), i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        searchField.removeTextChangedListener(textWatcher);
        searchField.setText(spannable);
        searchField.setSelection(spannable.length());
        searchField.addTextChangedListener(textWatcher);
        engineUiList.filter("");
        listView.invalidateViews();
    }

    private void checkForDeletions(Editable editable) {
        boolean hasDeletions = false;
        UserSpan[] spans = editable.getSpans(0, editable.length(), UserSpan.class);
        for (Integer u : selectedUsers) {
            boolean founded = false;
            for (UserSpan span : spans) {
                if (span.getUser().getId() == u) {
                    if (editable.getSpanStart(span) == editable.getSpanEnd(span)) {
                        break;
                    } else {
                        founded = true;
                        break;
                    }
                }
            }

            if (!founded) {
                hasDeletions = true;
                selectedUsers.remove(u);
            }
        }
        if (hasDeletions) {
            getActivity().invalidateOptionsMenu();
            listView.invalidateViews();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        searchField.removeTextChangedListener(textWatcher);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        engineUiList.getUiList().removeListener(this);
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        listView = null;
    }

    private class UserSpan extends ReplacementSpan {

        private UserModel user;
        private int maxW;
        private String userText;
        private TextPaint textPaint;

        private UserModel getUser() {
            return user;
        }

        public UserSpan(UserModel user, int maxW) {
            this.user = user;
            this.maxW = maxW;
            if (textPaint == null) {
                textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
                textPaint.setTextSize(Screen.sp(16));
                textPaint.setColor(getResources().getColor(R.color.text_primary));
            }

            int padding = Screen.dp(18);
            int maxWidth = maxW - padding;
            userText = TextUtils.ellipsize(user.getName(), textPaint, maxWidth, TextUtils.TruncateAt.END).toString();
        }

        @Override
        public int getSize(Paint paint, CharSequence charSequence, int start, int end, Paint.FontMetricsInt fm) {
            if (fm != null) {
                // WTF???
                fm.ascent = -Screen.dp(21 + 3);
                fm.descent = Screen.dp(10 + 3);

                fm.top = fm.ascent;
                fm.bottom = fm.descent;
            }
            return (int) textPaint.measureText(userText) + Screen.dp(24 + 8);
        }

        @Override
        public void draw(Canvas canvas, CharSequence charSequence, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            int size = (int) textPaint.measureText(userText);
            Paint debug = new Paint();
            debug.setColor(0xffebebeb);
            debug.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawRoundRect(new RectF(x + Screen.dp(4), y - Screen.dp(20), x + size + Screen.dp(4 + 24), y + Screen.dp(8)), Screen.dp(14), Screen.dp(14), debug);
            canvas.drawText(userText, x + Screen.dp(4 + 12), y, textPaint);
        }
    }
}
