/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ReduceStore } from 'flux/utils';
import Dispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, AsyncActionStates, PreferencesTabTypes } from '../constants/ActorAppConstants';

import ActorClient from '../utils/ActorClient';

class PreferencesStore extends ReduceStore {
  getInitialState() {
    return {
      sessions: [],
      currentTab: PreferencesTabTypes.GENERAL,
      terminateState: {}
    }
  }

  reduce(state, action) {
    switch(action.type) {
      case ActionTypes.PREFERENCES_SESSION_LOAD_SUCCESS:
        return {
          ...state,
          sessions: action.response
        }
      case ActionTypes.PREFERENCES_MODAL_HIDE:
        return this.getInitialState();

      case ActionTypes.PREFERENCES_CHANGE_TAB:
        return {
          ...state,
          currentTab: action.tab
        }

      case ActionTypes.PREFERENCES_SESSION_TERMINATE:
        state.terminateState[action.id] = AsyncActionStates.PROCESSING;
        return state;
      case ActionTypes.PREFERENCES_SESSION_TERMINATE_SUCCESS:
        delete state.terminateState[action.id];
        return state;
      case ActionTypes.PREFERENCES_SESSION_TERMINATE_ERROR:
        state.terminateState[action.id] = AsyncActionStates.FAILURE;
        return state;

      default:
        return state;
    }
  }

  isSendByEnterEnabled() {
    return ActorClient.isSendByEnterEnabled();
  }

  isGroupsNotificationsEnabled() {
    return ActorClient.isGroupsNotificationsEnabled();
  }

  isOnlyMentionNotifications() {
    return ActorClient.isOnlyMentionNotifications();
  }

  isSoundEffectsEnabled() {
    return ActorClient.isSoundEffectsEnabled();
  }

  isShowNotificationsTextEnabled() {
    return ActorClient.isShowNotificationsTextEnabled();
  }

  getSessions() {
    return this.getState().sessions;
  }

  getCurrentTab() {
    return this.getState().currentTab;
  }

  getTerminateState() {
    return this.getState().terminateState;
  }
}

export default new PreferencesStore(Dispatcher);
