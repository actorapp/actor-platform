/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

class DialogInfoStore extends ReduceStore {
  getInitialState() {
    return null;
  }

  reduce(state, action) {
    switch(action.type) {
      case ActionTypes.SELECT_DIALOG_PEER:
        if (action.peer.type === PeerTypes.GROUP) {
          return ActorClient.getGroup(action.peer.id);
        }

        return ActorClient.getUser(action.peer.id);

      case ActionTypes.DIALOG_INFO_CHANGED:
        return action.info;

      default:
        return state;
    }
  }
}

export default new DialogInfoStore(Dispatcher);
