/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

import ComposeActionCreators from '../actions/ComposeActionCreators';

export default {
  open() {
    dispatch(ActionTypes.CONTACT_LIST_SHOW);
    ComposeActionCreators.toggleAutoFocus(false);
  },

  close() {
    dispatch(ActionTypes.CONTACT_LIST_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  },

  setContacts(contacts) {
    dispatch(ActionTypes.CONTACT_LIST_CHANGED, { contacts })
  },

  addContact(uid) {
    return dispatchAsync(ActorClient.addContact(uid), {
      request: ActionTypes.CONTACT_ADD,
      success: ActionTypes.CONTACT_ADD_SUCCESS,
      failure: ActionTypes.CONTACT_ADD_ERROR
    }, { uid });
  },

  removeContact(uid) {
    return dispatchAsync(ActorClient.removeContact(uid), {
      request: ActionTypes.CONTACT_REMOVE,
      success: ActionTypes.CONTACT_REMOVE_SUCCESS,
      failure: ActionTypes.CONTACT_REMOVE_ERROR
    }, { uid });
  }
};
