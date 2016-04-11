/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import Immutable from 'immutable';
import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const INITIAL_MESSAGES_COUNT = 20;
const MESSAGE_COUNT_STEP = 20;

class MessageStore extends ReduceStore {
  getInitialState() {
    return {
      messages: [],
      overlay: [],
      isLoaded: false,
      receiveDate: 0,
      readDate: 0,
      count: INITIAL_MESSAGES_COUNT,
      selected: new Immutable.Set()
    };
  }

  getAll() {
    return this.getState().messages;
  }

  getRenderMessagesCount() {
    return this.getState().count;
  }

  getMessages() {
    return this.getState().messages;
  }

  getOverlay() {
    return this.getState().overlay;
  }

  isLoaded() {
    return this.getState().isLoaded;
  }

  isAllRendered() {
    const { messages, count } = this.getState();
    return messages.length === count;
  }

  getSelected() {
    return this.getState().selected;
  }

  reduce (state, action) {
    switch (action.type) {
      case ActionTypes.BIND_DIALOG_PEER:
        return {
          ...state,
          count: INITIAL_MESSAGES_COUNT,
          selected: state.selected.clear()
        };

      case ActionTypes.MESSAGES_CHANGED:
        return {
          ...state,
          messages: action.messages,
          overlay: action.overlay,
          isLoaded: action.isLoaded,
          receiveDate: action.receiveDate,
          readDate: action.readDate,
          count: Math.min(action.messages.length, state.count)
        };

      case ActionTypes.MESSAGES_SET_SELECTED:
        return {
          ...state,
          selected: action.selectedMesages
        };

      case ActionTypes.MESSAGES_LOAD_MORE:
        return {
          ...state,
          count: Math.min(state.messages.length, state.count + MESSAGE_COUNT_STEP)
        };

      default:
        return state;
    }
  }
}

export default new MessageStore(Dispatcher);
