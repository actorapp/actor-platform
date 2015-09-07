import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

import { english, russian } from 'l18n';

const CHANGE_EVENT = 'change';

let _isModalOpen = false,
    _languageData = null;

class PreferencesStore extends EventEmitter {
  constructor() {
    super();
  }

  emitChange() {
    this.emit(CHANGE_EVENT);
  }

  addChangeListener(callback) {
    this.on(CHANGE_EVENT, callback);
  }

  removeChangeListener(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }

  isModalOpen() {
    return _isModalOpen;
  }

  getSendByEnter() {
    return ActorClient.isSendByEnterEnabled();
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

  savePreferences(newPreferences) {
    const isSendByEnterEnabled = newPreferences.isSendByEnterEnabled === 'true';

    ActorClient.changeSendByEnter(isSendByEnterEnabled);

    PreferencesStoreInstance.emitChange();
  }
}

let PreferencesStoreInstance = new PreferencesStore();

PreferencesStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  switch(action.type) {
    case ActionTypes.PREFERENCES_MODAL_SHOW:
      _isModalOpen = true;
      PreferencesStoreInstance.emitChange();
      break;
    case ActionTypes.PREFERENCES_MODAL_HIDE:
      _isModalOpen = false;
      PreferencesStoreInstance.emitChange();
      break;

    case ActionTypes.PREFERENCES_SAVE:
      PreferencesStoreInstance.savePreferences(action.preferences);
      break;

    default:
      return;
  }
});

export default PreferencesStoreInstance;
