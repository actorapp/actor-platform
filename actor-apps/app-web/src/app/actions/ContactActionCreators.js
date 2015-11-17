/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorAppDispatcher, { dispatch } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  open: () => dispatch(ActionTypes.CONTACT_LIST_SHOW),
  close: () => dispatch(ActionTypes.CONTACT_LIST_HIDE),
  setContacts: (contacts) => dispatch(ActionTypes.CONTACT_LIST_CHANGED, { contacts }),
  addContact: (uid) => dispatch(ActionTypes.CONTACT_ADD, { uid }),
  removeContact: (uid) => dispatch(ActionTypes.CONTACT_REMOVE, { uid })
};
