package im.actor.messenger.app.fragment.media;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import im.actor.core.entity.GroupMember;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.Modules;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.BaseActivity;
import im.actor.messenger.app.activity.BaseFragmentActivity;
import im.actor.messenger.app.fragment.chat.mentions.MentionsAdapter;
import im.actor.messenger.app.fragment.chat.messages.MessagesFragment;
import im.actor.messenger.app.util.RandomUtil;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.MentionSpan;
import im.actor.messenger.app.view.SelectionListenerEditText;
import im.actor.messenger.app.view.TypingDrawable;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.messenger.app.view.markdown.AndroidMarkdown;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

import static im.actor.messenger.app.core.Core.groups;
import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.users;
import static im.actor.messenger.app.view.ViewUtils.expandMentions;
import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;
import static im.actor.messenger.app.view.emoji.SmileProcessor.emoji;

public class DocumentsActivity extends BaseFragmentActivity {

    private Peer peer;
    public static final String EXTRA_CHAT_PEER = "chat_peer";

    public static Intent build(Peer peer, Context context) {

        final Intent intent = new Intent(context, DocumentsActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        peer = Peer.fromUniqueId(getIntent().getExtras().getLong(EXTRA_CHAT_PEER));
        getSupportActionBar().setTitle("Documents");

        if (savedInstanceState == null) {
            showFragment(new DocumentsFragment(peer), false, false);
        }
    }

    @Override
    public ActionMode startSupportActionMode(final ActionMode.Callback callback) {
        // Fix for bug https://code.google.com/p/android/issues/detail?id=159527
        final ActionMode mode = super.startSupportActionMode(callback);
        if (mode != null) {
            mode.invalidate();
        }
        return mode;
    }
}

