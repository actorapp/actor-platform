import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

import { english, russian } from 'l18n';

const CHANGE_EVENT = 'change';

let _isModalOpen = false,
    _preferences = {},
    _sendByEnter = '',
    _language = '',
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

  get isModalOpen() {
    return _isModalOpen;
  }

  get preferences() {
    return _preferences;
  }

  get sendByEnter() {
    return _sendByEnter;
  }

  get language() {
    return _language;
  }

  get languageData() {
    switch (_language) {
      case 'ru':
        _languageData = russian;
        break;
      //case 'en':
      //  _languageData = english;
      //  break;
      default:
        _languageData = english;
        break;
    }

    return _languageData;
  }

  savePreferences(newPreferences) {
    localStorage['preferences.SEND_BY_ENTER'] = _sendByEnter = newPreferences.sendByEnter;
    localStorage['preferences.LANGUAGE'] = _language = newPreferences.language;

    _preferences = newPreferences;
  }

  loadPreferences() {
    _sendByEnter = localStorage.getItem('preferences.SEND_BY_ENTER') || 'true';
    _language = localStorage.getItem('preferences.LANGUAGE') || 'en';

    _preferences = {
      sendByEnter: _sendByEnter,
      language: _language
    };
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
      PreferencesStoreInstance.emitChange();
      break;

    case ActionTypes.PREFERENCES_LOAD:
      PreferencesStoreInstance.loadPreferences();
      PreferencesStoreInstance.emitChange();
      break;
    default:
      return;
  }
});

export default PreferencesStoreInstance;
