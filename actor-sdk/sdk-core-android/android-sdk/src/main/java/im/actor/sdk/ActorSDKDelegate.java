package im.actor.sdk;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import org.jetbrains.annotations.Nullable;

import im.actor.core.entity.Peer;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.controllers.conversation.ChatFragment;
import im.actor.sdk.controllers.conversation.attach.AbsAttachFragment;
import im.actor.sdk.controllers.conversation.inputbar.InputBarFragment;
import im.actor.sdk.controllers.conversation.mentions.AutocompleteFragment;
import im.actor.sdk.controllers.conversation.messages.content.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.quote.QuoteFragment;
import im.actor.sdk.controllers.settings.BaseGroupInfoActivity;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

/**
 * SDK Delegate. Used for delegating some work to
 */
public interface ActorSDKDelegate {

    /**
     * Intent for first authentication Activity. For default activity return null.
     *
     * @return Actor Intent
     */
    ActorIntent getAuthStartIntent();

    /**
     * Intent for activity that is launched after user log in. For start activity return null.
     *
     * @return Actor Intent
     */
    ActorIntent getStartAfterLoginIntent();

    /**
     * Intent for application start activity for logged in user. For default activity return null.
     *
     * @return Actor Intent
     */
    ActorIntent getStartIntent();

    /**
     * Optional Root Fragment
     *
     * @return Customized Fragment for root screen
     */
    @Nullable
    Fragment fragmentForRoot();

    /**
     * If not null returned, overrides users profile fragment
     *
     * @param uid user id
     * @return Fragment
     */
    @Nullable
    Fragment fragmentForProfile(int uid);

    /**
     * If not null returned, overrides group info fragment
     *
     * @return Actor Intent
     */
    Fragment fragmentForGroupInfo(int gid);


    //
    // Chat
    //

    /**
     * If Not null returned, overrides attachment menu
     *
     * @param peer peer
     * @return Subclass from AbsAttachFragment
     */
    @Nullable
    AbsAttachFragment fragmentForAttachMenu(Peer peer);

    /**
     * If Not null returned, overrides chat fragment
     *
     * @param peer peer
     * @return Custom chat fragment
     */
    @Nullable
    ChatFragment fragmentForChat(Peer peer);

    /**
     * If Not null returned, overrides chat input fragment
     *
     * @return Custom chat input fragment
     */
    @Nullable
    InputBarFragment fragmentForChatInput();

    /**
     * If Not null returned, overrides chat autocomplete fragment
     *
     * @return Custom chat autocomplete fragment
     * @param peer peer
     */
    AutocompleteFragment fragmentForAutocomplete(Peer peer);

    /**
     * If Not null returned, overrides chat quote fragment
     *
     * @return Custom chat quote fragment
     */
    QuoteFragment fragmentForQuote();

    /**
     * If Not null returned, overrides default toolbar (no-ui) fragment
     *
     * @param peer peer
     * @return Custom Toolbar fragment
     */
    @Nullable
    Fragment fragmentForToolbar(Peer peer);


    /**
     * Override for hacking default messages view holders
     *
     * @param base base view holder class
     * @param args args passed to view holder
     * @param <T>  base view holder class
     * @param <J>  return class
     * @return hacked view holder
     */
    <T extends BindedViewHolder, J extends T> J getViewHolder(Class<T> base, Object... args);

    /**
     * Override for hacking custom messages view holders
     *
     * @param dataTypeHash    json dataType hash
     * @param messagesAdapter adapter to pass to holder
     * @param viewGroup       ViewGroup to pass to holder
     * @return custom view holder
     */
    MessageHolder getCustomMessageViewHolder(int dataTypeHash, MessagesAdapter messagesAdapter, ViewGroup viewGroup);


    //
    // Settings
    //

    /**
     * If not null returned, overrides settings activity intent
     *
     * @return Actor Intent
     */
    ActorIntentFragmentActivity getSettingsIntent();


    /**
     * If not null returned, overrides settings activity intent
     *
     * @return Actor Intent
     */
    ActorIntentFragmentActivity getChatSettingsIntent();

    /**
     * If not null returned, overrides security settings activity intent
     *
     * @return Actor Intent
     */
    ActorIntentFragmentActivity getSecuritySettingsIntent();

    /**
     * If not null returned, overrides chat activity intent
     *
     * @param peer    chat peer
     * @param compose pop up keyboard at start
     * @return Actor Intent
     */
    ActorIntent getChatIntent(Peer peer, boolean compose);


    //
    // Custom Notifications
    //

    /**
     * Override for setting specific notification sound for peer
     *
     * @param peer peer to set notification sound
     * @return notification sound uri
     */
    Uri getNotificationSoundForPeer(Peer peer);


    /**
     * Override for setting specific notification color for peer
     *
     * @param peer peer to set notification color
     * @return notification sound color
     */
    int getNotificationColorForPeer(Peer peer);


    /**
     * Override change notification sound
     *
     * @return notification sound uri
     */
    Uri getNotificationSound();

    /**
     * Override change notification color
     *
     * @return notification sound color
     */
    int getNotificationColor();


}
