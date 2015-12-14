/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

let _info = null;

class DialogInfoStore extends Store {
  getInfo() {
    return _info;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.SELECT_DIALOG_PEER:
        if (action.peer.type === PeerTypes.GROUP) {
          _info = ActorClient.getGroup(action.peer.id);
        } else if (action.peer.type === PeerTypes.USER) {
          _info = ActorClient.getUser(action.peer.id);
        }
        this.__emitChange();
        break;
      case ActionTypes.DIALOG_INFO_CHANGED:
        _info = action.info;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new DialogInfoStore(Dispatcher);
