package im.actor.sdk;

import android.net.Uri;
import android.support.v4.app.Fragment;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.core.RawUpdatesHandler;
import im.actor.core.entity.Peer;
import im.actor.sdk.controllers.conversation.ChatFragment;
import im.actor.sdk.controllers.conversation.attach.AbsAttachFragment;
import im.actor.sdk.controllers.conversation.inputbar.InputBarFragment;
import im.actor.sdk.controllers.conversation.mentions.AutocompleteFragment;
import im.actor.sdk.controllers.conversation.messages.BubbleLayouter;
import im.actor.sdk.controllers.conversation.quote.QuoteFragment;
import im.actor.sdk.controllers.dialogs.DialogsDefaultFragment;
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
     * If not null returned, overrides call fragment
     *
     * @param callId call id
     * @return Fragment
     */
    @Nullable
    Fragment fragmentForCall(long callId);

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

    /**
     * If not null returned, overrides raw updates handler actor
     *
     * @return RawUpdatesHandler actor
     */
    RawUpdatesHandler getRawUpdatesHandler();

    /**
     * Override/add new messages view holders
     *
     * @param layouters default layouters
     */
    void configureChatViewHolders(ArrayList<BubbleLayouter> layouters);

    DialogsDefaultFragment fragmentForDialogs();
}
