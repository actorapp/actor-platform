import { EventEmitter } from 'events';
import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

import ActorClient from 'utils/ActorClient';
import mixpanel from 'utils/Mixpanel';

const CHANGE_EVENT = 'change';

let _profile = null,
    _name = null,
    _nick = null,
    _isModalOpen = false;

class MyProfileStore extends EventEmitter {
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

  getName() {
    return _name;
  }

  getNick() {
    return _nick;
  }

  getProfile() {
    return _profile;
  }
}

const setProfile = (profile) => {
  _profile = profile;
  _name = profile.name;
  _nick = profile.nick;
};

let MyProfileStoreInstance = new MyProfileStore();

MyProfileStoreInstance.dispatchToken = ActorAppDispatcher.register(action => {
  switch(action.type) {
    case ActionTypes.MY_PROFILE_MODAL_SHOW:
      ActorClient.bindUser(ActorClient.getUid(), setProfile);
      _isModalOpen = true;
      MyProfileStoreInstance.emitChange();
      break;
    case ActionTypes.MY_PROFILE_MODAL_HIDE:
      ActorClient.unbindUser(ActorClient.getUid(), setProfile);
      _isModalOpen = false;
      MyProfileStoreInstance.emitChange();
      break;
    case ActionTypes.MY_PROFILE_SAVE_NAME:
      _name = action.name;
      ActorClient.editMyName(_name);
      MyProfileStoreInstance.emitChange();
      break;
    case ActionTypes.MY_PROFILE_SAVE_NICKNAME:
      _nick = action.nick;
      ActorClient.editMyNick(_nick);
      MyProfileStoreInstance.emitChange();
      break;
    default:
      return;
  }
});

export default MyProfileStoreInstance;
