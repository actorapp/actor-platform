/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import Immutable from 'immutable';
import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, MessageChangeReason } from '../constants/ActorAppConstants';

const MESSAGE_COUNT_STEP = 20;

const getMessageId = (message) => message ? message.rid : null;

class MessageStore extends ReduceStore {
  getInitialState() {
    return {
      messages: [],
      overlay: [],
      isLoaded: false,
      receiveDate: 0,
      readDate: 0,
      readByMeDate: 0,
      count: 0,
      firstMessageId: null,
      lastMessageId: null,
      changeReason: MessageChangeReason.UNKNOWN,
      selected: new Immutable.Set()
    };
  }

  isAllRendered() {
    const { messages, count } = this.getState();
    return messages.length === count;
  }

  reduce (state, action) {
    switch (action.type) {
      case ActionTypes.BIND_DIALOG_PEER:
        return this.getInitialState();

      case ActionTypes.MESSAGES_CHANGED:
        const firstMessageId = getMessageId(action.messages[0]);
        const lastMessageId = getMessageId(action.messages[action.messages.length - 1]);

        if (firstMessageId !== state.firstMessageId) {
          return {
            ...state,
            firstMessageId,
            lastMessageId,
            messages: action.messages,
            overlay: action.overlay,
            receiveDate: action.receiveDate,
            readDate: action.readDate,
            readByMeDate: action.readByMeDate,
            isLoaded: action.isLoaded,
            count: Math.min(action.messages.length, state.count + MESSAGE_COUNT_STEP),
            changeReason: MessageChangeReason.UNSHIFT
          };
        }

        if (lastMessageId !== state.lastMessageId) {
          return {
            ...state,
            firstMessageId,
            lastMessageId,
            messages: action.messages,
            overlay: action.overlay,
            receiveDate: action.receiveDate,
            readDate: action.readDate,
            readByMeDate: action.readByMeDate,
            isLoaded: action.isLoaded,
            count: Math.min(action.messages.length, state.count + action.messages.length - state.messages.length),
            changeReason: MessageChangeReason.PUSH
          };
        }

        return {
          ...state,
          firstMessageId,
          lastMessageId,
          messages: action.messages,
          overlay: action.overlay,
          receiveDate: action.receiveDate,
          readDate: action.readDate,
          readByMeDate: action.readByMeDate,
          isLoaded: action.isLoaded,
          count: Math.min(action.messages.length, state.count),
          changeReason: MessageChangeReason.UPDATE
        };

      case ActionTypes.MESSAGES_LOAD_MORE:
        return {
          ...state,
          count: Math.min(state.messages.length, state.count + MESSAGE_COUNT_STEP),
          changeReason: MessageChangeReason.UNSHIFT
        };

      case ActionTypes.MESSAGES_TOGGLE_SELECTED:
        return {
          ...state,
          selected: state.selected.has(action.id) ? state.selected.remove(action.id) : state.selected.add(action.id)
        };

      default:
        return state;
    }
  }
}

export default new MessageStore(Dispatcher);
