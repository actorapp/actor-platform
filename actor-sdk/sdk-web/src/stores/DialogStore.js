/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { find, some } from 'lodash';
import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

class DialogStore extends Store {
  constructor(dispatcher) {
    super(dispatcher);

    this.dialogs = [];
    this.currentPeer = null;
    this.lastPeer = null;
  }

  getDialogs() {
    return this.dialogs;
  }

  getCurrentPeer() {
    return this.currentPeer;
  }

  getLastPeer() {
    return this.lastPeer;
  }

  isMember() {
    if (this.currentPeer !== null && this.currentPeer.type === PeerTypes.GROUP) {
      const group = ActorClient.getGroup(this.currentPeer.id);
      return group && group.members.length !== 0;
    }

    return true;
  }

  isFavorite(id) {
    const favoriteDialogs = find(this.dialogs, {key: 'favourites'});
    if (!favoriteDialogs) return false;

    return some(favoriteDialogs.shorts, (dialog) => dialog.peer.peer.id === id);
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.DIALOGS_CHANGED:
        this.dialogs = action.dialogs;
        this.__emitChange();
        break;
      case ActionTypes.BIND_DIALOG_PEER:
        this.lastPeer = this.currentPeer;
        this.currentPeer = action.peer;
        this.__emitChange();
        break;
      case ActionTypes.UNBIND_DIALOG_PEER:
        this.lastPeer = this.currentPeer;
        this.currentPeer = null;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new DialogStore(Dispatcher);
