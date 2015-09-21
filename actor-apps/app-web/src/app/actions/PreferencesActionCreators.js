/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

export default {
  show() {
    dispatch(ActionTypes.PREFERENCES_MODAL_SHOW);
    this.loadSessions();
  },

  hide() {
    dispatch(ActionTypes.PREFERENCES_MODAL_HIDE);
  },

  save(preferences) {
    dispatch(ActionTypes.PREFERENCES_SAVE, {
      preferences
    });
  },

  changeTab(tab) {
    dispatch(ActionTypes.PREFERENCES_CHANGE_TAB, {
      tab
    });
  },

  loadSessions() {
    dispatchAsync(ActorClient.loadSessions(), {
      request: ActionTypes.PREFERENCES_SESSION_LOAD,
      success: ActionTypes.PREFERENCES_SESSION_LOAD_SUCCESS,
      failure: ActionTypes.PREFERENCES_SESSION_LOAD_ERROR
    });
  },

  terminateSession(id) {
    dispatchAsync(ActorClient.terminateSession(id), {
      request: ActionTypes.PREFERENCES_SESSION_TERMINATE,
      success: ActionTypes.PREFERENCES_SESSION_TERMINATE_SUCCESS,
      failure: ActionTypes.PREFERENCES_SESSION_TERMINATE_ERROR
    }, { id }).then(
      // Reload active session list
      () => this.loadSessions()
    );
  },

  terminateAllSessions() {
    dispatchAsync(ActorClient.terminateAllSessions(), {
      request: ActionTypes.PREFERENCES_SESSION_TERMINATE_ALL,
      success: ActionTypes.PREFERENCES_SESSION_TERMINATE_ALL_SUCCESS,
      failure: ActionTypes.PREFERENCES_SESSION_TERMINATE_ALL_ERROR
    }).then(
      // Reload active session list
      () => this.loadSessions()
    );
  }
};
