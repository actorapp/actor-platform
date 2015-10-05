package im.actor.core.modules;

import im.actor.core.Configuration;
import im.actor.core.Extension;
import im.actor.core.Messenger;
import im.actor.core.i18n.I18nEngine;
import im.actor.core.modules.internal.AppStateModule;
import im.actor.core.modules.internal.ContactsModule;
import im.actor.core.modules.internal.DisplayLists;
import im.actor.core.modules.internal.ExternalModule;
import im.actor.core.modules.internal.FilesModule;
import im.actor.core.modules.internal.GroupsModule;
import im.actor.core.modules.internal.MentionsModule;
import im.actor.core.modules.internal.MessagesModule;
import im.actor.core.modules.internal.NotificationsModule;
import im.actor.core.modules.internal.PresenceModule;
import im.actor.core.modules.internal.ProfileModule;
import im.actor.core.modules.internal.PushesModule;
import im.actor.core.modules.internal.SearchModule;
import im.actor.core.modules.internal.SecurityModule;
import im.actor.core.modules.internal.SettingsModule;
import im.actor.core.modules.internal.TypingModule;
import im.actor.core.modules.internal.UsersModule;
import im.actor.core.network.ActorApi;
import im.actor.runtime.eventbus.EventBus;
import im.actor.runtime.storage.PreferencesStorage;

public interface ModuleContext {

    // Messenger Configuration
    Configuration getConfiguration();

    // API Access
    ActorApi getActorApi();

    ApiModule getApiModule();

    // Preferences
    PreferencesStorage getPreferences();

    // Built-In modules
    Authentication getAuthModule();

    // Event Bus
    EventBus getEvents();

    UsersModule getUsersModule();

    GroupsModule getGroupsModule();

    MessagesModule getMessagesModule();

    Updates getUpdatesModule();

    TypingModule getTypingModule();

    PresenceModule getPresenceModule();

    I18nEngine getI18nModule();

    ContactsModule getContactsModule();

    FilesModule getFilesModule();

    NotificationsModule getNotificationsModule();

    SettingsModule getSettingsModule();

    ProfileModule getProfileModule();

    AppStateModule getAppStateModule();

    PushesModule getPushesModule();

    SecurityModule getSecurityModule();

    SearchModule getSearchModule();

    ExternalModule getExternalModule();

    DisplayLists getDisplayListsModule();

    Messenger getMessenger();

    MentionsModule getMentions();

    Extension findExtension(String key);
}
