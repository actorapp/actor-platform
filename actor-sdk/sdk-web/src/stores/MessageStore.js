/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import Immutable from 'immutable';
import { ReduceStore } from 'flux/utils';
import { last } from 'lodash';
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
      isLoading: false,
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
        if (action.messages[0] !== state.messages[0]) {
          // unshifted new messages
          return {
            ...state,
            messages: action.messages,
            overlay: action.overlay,
            receiveDate: action.receiveDate,
            readDate: action.readDate,
            isLoaded: action.isLoaded,
            isLoading: false,
            count: Math.min(action.messages.length, state.count + MESSAGE_COUNT_STEP)
          };
        }

        if (last(action.messages) !== last(state.messages)) {
          // pushed new messages
          return {
            ...state,
            messages: action.messages,
            overlay: action.overlay,
            receiveDate: action.receiveDate,
            readDate: action.readDate,
            isLoaded: action.isLoaded,
            count: Math.min(action.messages.length, state.count + action.messages.length - state.messages.length)
          };
        }

        return {
          ...state,
          messages: action.messages,
          overlay: action.overlay,
          receiveDate: action.receiveDate,
          readDate: action.readDate,
          isLoaded: action.isLoaded,
          count: Math.min(action.messages.length, INITIAL_MESSAGES_COUNT)
        };

      case ActionTypes.MESSAGES_TOGGLE_SELECTED:
        return {
          ...state,
          selected: state.selected.has(action.id) ? state.selected.remove(action.id) : state.selected.add(action.id)
        };

      case ActionTypes.MESSAGES_LOADING_MORE:
        return {
          ...state
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
