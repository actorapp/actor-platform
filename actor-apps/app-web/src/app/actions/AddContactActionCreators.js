/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import ActorClient from 'utils/ActorClient';
import { ActionTypes } from 'constants/ActorAppConstants';
import ContactActionCreators from 'actions/ContactActionCreators';
import DialogActionCreators from 'actions/DialogActionCreators';

export default {
  open: () => dispatch(ActionTypes.CONTACT_ADD_MODAL_SHOW),
  close: () => dispatch(ActionTypes.CONTACT_ADD_MODAL_HIDE),

  findUsers: (query) => {
    // TODO: Use dispatchAsync method
    ActorClient.findUsers(query)
      .then(users => {
        if (users.length > 0) {
          const user = users[0];
          const uid = user.id;
          const userPeer = ActorClient.getUserPeer(uid);

          if (user.isContact) {
            DialogActionCreators.selectDialogPeer(userPeer);
            dispatch(ActionTypes.CONTACT_ADD_MODAL_FIND_USER_IN_CONTACT);
          } else {
            ContactActionCreators.addContact(uid);
            DialogActionCreators.selectDialogPeer(userPeer);
            dispatch(ActionTypes.CONTACT_ADD_MODAL_FIND_USER_OK);
          }
        } else {
          dispatch(ActionTypes.CONTACT_ADD_MODAL_FIND_USER_UNREGISTERED);
        }
      }).catch(error => {
      throw new Error(error);
    });
  }
};
