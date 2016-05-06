/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, PeerTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

function getPeer(peer) {
  if (peer.type === PeerTypes.GROUP) {
    return ActorClient.getGroup(peer.id);
  }

  return ActorClient.getUser(peer.id);
}

class DialogInfoStore extends ReduceStore {
  getInitialState() {
    // Temporary workaround while isStarted isn't correct
    this.__isStarted = false;
    return null;
  }

  reduce(state, action) {
    switch (action.type) {
      case ActionTypes.SELECT_DIALOG_PEER:
        const info = getPeer(action.peer);

        return {
          ...info,
          isStarted: this.__isStarted
        };

      case ActionTypes.DIALOG_INFO_CHANGED:
        return {
          ...action.info,
          isStarted: this.__isStarted
        };

      case ActionTypes.MESSAGES_CHANGED:
        this.__isStarted = action.messages && action.messages.length > 0;
        if (state) {
          return {
            ...state,
            isStarted: this.__isStarted
          };
        }

        return state;

      default:
        return state;
    }
  }
}

export default new DialogInfoStore(Dispatcher);
