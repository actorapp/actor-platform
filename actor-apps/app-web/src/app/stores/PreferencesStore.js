import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

const CHANGE_EVENT = 'change';

let _isModalOpen = false,
    _preferences = {},
    _sendByEnter = '';//,
    //_language = '';

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

  //get language() {
  //  return _language;
  //}

  savePreferences(newPreferences) {
    //console.info('savePreferences', newPreferences);
    localStorage['preferences.send_by_enter'] = _sendByEnter = newPreferences.sendByEnter;
    //localStorage['preferences.language'] = newPreferences.language;
    _preferences = newPreferences;
  }

  loadPreferences() {
    _sendByEnter = localStorage['preferences.send_by_enter'];
    //_language = localStorage['preferences.language'];

    _preferences = {
      sendByEnter: _sendByEnter
      //language: _language,
    };

    //console.info('loadPreferences', _preferences);
    //return _preferences;
  }
}

let PreferencesStoreInstance = new PreferencesStore();

PreferencesStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  console.info(action);
  switch(action.type) {
    case ActionTypes.PREFERENCES_MODAL_SHOW:
      _isModalOpen = true;
      break;
    case ActionTypes.PREFERENCES_MODAL_HIDE:
      _isModalOpen = false;
      break;

    case ActionTypes.PREFERENCES_SAVE:
      PreferencesStoreInstance.savePreferences(action.preferences);
      break;

    case ActionTypes.SET_LOGGED_IN:
      PreferencesStoreInstance.loadPreferences();
      break;
    default:
      return;
  }
  PreferencesStoreInstance.emitChange();
});

export default PreferencesStoreInstance;
