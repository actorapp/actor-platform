/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

let _profile = null,
    _name = null,
    _nick = null,
    _about = null,
    _isModalOpen = false;

class MyProfileStore extends Store {
  isModalOpen() {
    return _isModalOpen;
  }

  getName() {
    return _name;
  }

  getNick() {
    return _nick;
  }

  getAbout() {
    return _about;
  }

  getProfile() {
    return _profile;
  }

  setProfile(profile) {
    _profile = profile;
    _name = profile.name;
    _nick = profile.nick;
    _about = profile.about;
  };

  __onDispatch = (action) => {
    switch(action.type) {
      case ActionTypes.MY_PROFILE_MODAL_SHOW:
        _isModalOpen = true;
        this.__emitChange();
        break;
      case ActionTypes.MY_PROFILE_MODAL_HIDE:
        _isModalOpen = false;
        this.__emitChange();
        break;
      case ActionTypes.MY_PROFILE_CHANGED:
        this.setProfile(action.profile);
        this.__emitChange();
        break;
      case ActionTypes.MY_PROFILE_SAVE_NAME:
        if (_name !== action.name) {
          _name = action.name;
          ActorClient.editMyName(_name);
          this.__emitChange();
        }
        break;
      case ActionTypes.MY_PROFILE_SAVE_NICKNAME:
        if (_nick !== action.nick) {
          _nick = action.nick;
          ActorClient.editMyNick(_nick);
          this.__emitChange();
        }
        break;
      case ActionTypes.MY_PROFILE_EDIT_ABOUT_SUCCESS:
        if (_about !== action.about) {
          _about = action.about;
          this.__emitChange();
        }
        break;
      default:
    }
  }
}

export default new MyProfileStore(Dispatcher);
