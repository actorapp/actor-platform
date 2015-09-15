/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

import ActorClient from 'utils/ActorClient';

import { english, russian } from 'l18n';

let _isOpen = false,
    _languageData = null,
    _sessions = [];

class PreferencesStore extends Store {
  isOpen() {
    return _isOpen;
  }

  isSendByEnterEnabled() {
    return ActorClient.isSendByEnterEnabled();
  }

  isGroupsNotificationsEnabled() {
    return ActorClient.isGroupsNotificationsEnabled();
  }

  isOnlyMentionNotifications() {
    return ActorClient.isOnlyMentionNotifications();
  }

  isSoundEffectsEnabled() {
    return ActorClient.isSoundEffectsEnabled();
  }

  isNotificationTextPreviewEnabled() {
    return ActorClient.isNotificationTextPreviewEnabled();
  }

  getLanguageData() {
    switch (navigator.language) {
      case 'ru-RU':
      case 'ru':
        _languageData = russian;
        break;
      default:
        _languageData = english;
        break;
    }
    return _languageData;
  }

  getSessions() {
    return _sessions;
  }

  savePreferences(newPreferences) {
    const {
      isSendByEnterEnabled,
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications,
      isNotificationTextPreviewEnabled
    } = newPreferences;

    ActorClient.changeSendByEnter(isSendByEnterEnabled);
    ActorClient.changeSoundEffectsEnabled(isSoundEffectsEnabled);
    ActorClient.changeGroupNotificationsEnabled(isGroupsNotificationsEnabled);
    ActorClient.changeIsOnlyMentionNotifications(isOnlyMentionNotifications);
    ActorClient.changeNotificationTextPreviewEnabled(isNotificationTextPreviewEnabled);
  }

  __onDispatch = (action) => {
    switch(action.type) {
      case ActionTypes.PREFERENCES_MODAL_SHOW:
        _isOpen = true;
        this.__emitChange();
        break;
      case ActionTypes.PREFERENCES_MODAL_HIDE:
        _isOpen = false;
        this.__emitChange();
        break;
      case ActionTypes.PREFERENCES_SAVE:
        this.savePreferences(action.preferences);
        this.__emitChange();
        break;
      case ActionTypes.PREFERENCES_SESSION_LOAD_SUCCESS:
        _sessions = action.response;
        this.__emitChange();
        break;
      default:
    }
  }
}
export default new PreferencesStore(Dispatcher);
