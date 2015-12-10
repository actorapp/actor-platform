/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

let _dialogs = [],
    _currentPeer = null,
    _lastPeer = null,
    _info = null,
    _typing = null;

class DialogStore extends Store {
  getAllDialogs() {
    return _dialogs;
  }

  getCurrentPeer() {
    return _currentPeer;
  }

  getLastPeer() {
    return _lastPeer;
  }

  getInfo() {
    return _info;
  }

  getTyping() {
    return _typing;
  }

  isNotificationsEnabled(peer) {
    return ActorClient.isNotificationsEnabled(peer);
  }

  isMember() {
    if (_currentPeer !== null && _currentPeer.type === PeerTypes.GROUP) {
      const group = ActorClient.getGroup(_currentPeer.id);
      return group.members.length !== 0;
    } else {
      return true;
    }
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.SELECT_DIALOG_PEER:
        _lastPeer = _currentPeer;
        _currentPeer = action.peer;
        this.__emitChange();
        break;
      case ActionTypes.DIALOG_INFO_CHANGED:
        _info = action.info;
        this.__emitChange();
        break;
      case ActionTypes.DIALOG_TYPING_CHANGED:
        _typing = action.typing;
        this.__emitChange();
        break;
      case ActionTypes.DIALOGS_CHANGED:
        _dialogs = action.dialogs;
        this.__emitChange();
        break;
      case ActionTypes.NOTIFICATION_CHANGE:
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new DialogStore(Dispatcher);
