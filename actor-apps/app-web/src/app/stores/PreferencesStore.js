/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates } from 'constants/ActorAppConstants';

import ActorClient from 'utils/ActorClient';

import { english, russian } from 'l18n';

let _isOpen = false,
    _languageData = null,
    _sessions = [],
    _currentTab = 'GENERAL',
    _terminateSessionState = [];

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

  isShowNotificationsTextEnabled() {
    return ActorClient.isShowNotificationsTextEnabled();
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

  getCurrentTab() {
    return _currentTab;
  }

  getTerminateSessionState(id) {
    return (_terminateSessionState[id] || AsyncActionStates.PENDING);
  }


  savePreferences(newPreferences) {
    const {
      isSendByEnterEnabled,
      isSoundEffectsEnabled,
      isGroupsNotificationsEnabled,
      isOnlyMentionNotifications,
      isShowNotificationsTextEnabled
    } = newPreferences;

    ActorClient.changeSendByEnter(isSendByEnterEnabled);
    ActorClient.changeSoundEffectsEnabled(isSoundEffectsEnabled);
    ActorClient.changeGroupNotificationsEnabled(isGroupsNotificationsEnabled);
    ActorClient.changeIsOnlyMentionNotifications(isOnlyMentionNotifications);
    ActorClient.changeIsShowNotificationTextEnabled(isShowNotificationsTextEnabled);
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
      case ActionTypes.PREFERENCES_CHANGE_TAB:
        _currentTab = action.tab;
        this.__emitChange();
        break;

      case ActionTypes.PREFERENCES_SESSION_TERMINATE:
        _terminateSessionState[action.id] = AsyncActionStates.PROCESSING;
        this.__emitChange();
        break;
      case ActionTypes.PREFERENCES_SESSION_TERMINATE_SUCCESS:
        delete _terminateSessionState[action.id];
        this.__emitChange();
        break;
      case ActionTypes.PREFERENCES_SESSION_TERMINATE_ERROR:
        _terminateSessionState[action.id] = AsyncActionStates.FAILURE;
        this.__emitChange();
        break;


      default:
    }
  }
}
export default new PreferencesStore(Dispatcher);
