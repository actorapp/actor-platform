/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorAppDispatcher, { dispatch } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import mixpanel from 'utils/Mixpanel';

export default {
  showContactList: () => dispatch(ActionTypes.CONTACT_LIST_SHOW),
  hideContactList: () => dispatch(ActionTypes.CONTACT_LIST_HIDE),

  setContacts: (contacts) => dispatch(ActionTypes.CONTACT_LIST_CHANGED, { contacts }),

  addContact: (uid) => {
    mixpanel.track('Add user to contacts');
    dispatch(ActionTypes.CONTACT_ADD, { uid });
  },

  removeContact: (uid) => {
    mixpanel.track('Remove user from contacts');
    dispatch(ActionTypes.CONTACT_REMOVE, { uid });
  }
};
