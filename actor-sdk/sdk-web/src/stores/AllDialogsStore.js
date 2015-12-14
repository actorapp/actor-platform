/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { Store } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

let _dialogs = [];

class DialogStore extends Store {
  getAllDialogs() {
    return _dialogs;
  }

  __onDispatch(action) {
    switch(action.type) {
      case ActionTypes.DIALOGS_CHANGED:
        _dialogs = action.dialogs;
        this.__emitChange();
        break;
      default:
    }
  }
}

export default new DialogStore(Dispatcher);
