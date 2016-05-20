package im.actor.core.modules;

import im.actor.core.Configuration;
import im.actor.core.Messenger;
import im.actor.core.i18n.I18nEngine;
import im.actor.core.modules.api.ApiModule;
import im.actor.core.modules.auth.Authentication;
import im.actor.core.modules.eventbus.EventBusModule;
import im.actor.core.modules.sequence.Updates;
import im.actor.core.modules.misc.AppStateModule;
import im.actor.core.modules.calls.CallsModule;
import im.actor.core.modules.contacts.ContactsModule;
import im.actor.core.modules.misc.DeviceInfoModule;
import im.actor.core.modules.misc.DisplayLists;
import im.actor.core.modules.encryption.EncryptionModule;
import im.actor.core.modules.external.ExternalModule;
import im.actor.core.modules.file.FilesModule;
import im.actor.core.modules.groups.GroupsModule;
import im.actor.core.modules.mentions.MentionsModule;
import im.actor.core.modules.messaging.MessagesModule;
import im.actor.core.modules.notifications.NotificationsModule;
import im.actor.core.modules.presence.PresenceModule;
import im.actor.core.modules.profile.ProfileModule;
import im.actor.core.modules.push.PushesModule;
import im.actor.core.modules.search.SearchModule;
import im.actor.core.modules.security.SecurityModule;
import im.actor.core.modules.settings.SettingsModule;
import im.actor.core.modules.stickers.StickersModule;
import im.actor.core.modules.storage.StorageModule;
import im.actor.core.modules.typing.TypingModule;
import im.actor.core.modules.users.UsersModule;
import im.actor.core.network.ActorApi;
import im.actor.runtime.eventbus.EventBus;
import im.actor.runtime.storage.PreferencesStorage;

public interface ModuleContext {

    // Messenger Configuration
    Configuration getConfiguration();

    // API Access
    ActorApi getActorApi();

    ApiModule getApiModule();

    StorageModule getStorageModule();

    void afterStorageReset();

    // Preferences
    PreferencesStorage getPreferences();

    // Built-In modules
    Authentication getAuthModule();

    // Event Bus
    EventBus getEvents();

    UsersModule getUsersModule();

    GroupsModule getGroupsModule();

    StickersModule getStickersModule();

    CallsModule getCallsModule();

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

    DeviceInfoModule getDeviceInfoModule();

    EncryptionModule getEncryption();

    EventBusModule getEventBus();
}
