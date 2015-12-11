/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

export default {
  open() {
    dispatch(ActionTypes.CONTACT_LIST_SHOW)
  },

  close() {
    dispatch(ActionTypes.CONTACT_LIST_HIDE)
  },

  setContacts(contacts) {
    dispatch(ActionTypes.CONTACT_LIST_CHANGED, { contacts })
  },

  addContact(uid) {
    ActorClient.addContact(uid);
    dispatch(ActionTypes.CONTACT_ADD, { uid });
  },

  removeContact(uid) {
    ActorClient.removeContact(uid);
    dispatch(ActionTypes.CONTACT_REMOVE, { uid });
  },

  search(query) {
    dispatch(ActionTypes.CONTACT_LIST_SEARCH, { query })
  }
};
