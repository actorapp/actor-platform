/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import Immutable from 'immutable';
import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, MessageChangeReason } from '../constants/ActorAppConstants';
import { getFirstUnreadMessageIndex } from '../utils/MessageUtils';
import UserStore from './UserStore';

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
      firstUnreadId: null,
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

        const nextState = {
          ...state,
          firstMessageId,
          lastMessageId,
          messages: action.messages,
          overlay: action.overlay,
          receiveDate: action.receiveDate,
          readDate: action.readDate,
          readByMeDate: action.readByMeDate,
          isLoaded: action.isLoaded
        };

        if (firstMessageId !== state.firstMessageId) {
          nextState.count = Math.min(action.messages.length, state.count + MESSAGE_COUNT_STEP);
          nextState.changeReason = MessageChangeReason.UNSHIFT;
        } else if (lastMessageId !== state.lastMessageId) {
          // TODO: possible incorrect
          const lengthDiff = action.messages.length - state.messages.length;

          nextState.count = Math.min(action.messages.length, state.count + lengthDiff);
          nextState.changeReason = MessageChangeReason.PUSH;
        } else {
          nextState.count = Math.min(action.messages.length, state.count);
          nextState.changeReason = MessageChangeReason.UPDATE;
        }

        if (state.readByMeDate === 0 && action.readByMeDate > 0) {
          const unreadIndex = getFirstUnreadMessageIndex(action.messages, action.readByMeDate, UserStore.getMyId());
          if (unreadIndex === -1) {
            nextState.firstUnreadId = null;
          } else {
            nextState.firstUnreadId = action.messages[unreadIndex].rid;
            if (unreadIndex > nextState.count) {
              nextState.count = Math.min((action.messages.length - unreadIndex) + MESSAGE_COUNT_STEP, action.messages.length);
            }
          }
        }

        return nextState;

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
